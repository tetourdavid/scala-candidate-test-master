package com.letgo.scala_candidate_test.infrastructure

import java.time.Instant
import java.time.temporal.ChronoUnit.MILLIS

import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}
import com.letgo.scala_candidate_test.domain.{Tweet, TweetRepository}
import com.letgo.scala_candidate_test.infrastructure.TweetClient.UserNotFoundException

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

class TweetMemoryRepository(client: TweetRepository, expiration: FiniteDuration, eviction: FiniteDuration, capacity: Int)
                           (implicit ec: ExecutionContext, actorSystem: ActorSystem) extends TweetRepository {

  protected val store: mutable.HashMap[String, Record] = mutable.HashMap()

  override def searchByUserName(username: String, limit: Int): Future[Seq[Tweet]] = {
    store.get(username) match {
      case Some(Record(tweets, stamp)) if isFresh(stamp) && tweets.size >= limit =>
        Future.successful(tweets take limit)
      case _ =>
        load(username, limit)
    }
  }

  /** @throws UserNotFoundException when such username is not recognized by Twitter */
  private def load(username: String, limit: Int) = {
    val tweets = client.searchByUserName(username, limit)
    tweets foreach { tweets => if (bellowCapacity) store.put(username, Record(tweets))}
    tweets
  }

  private def bellowCapacity = store.size < capacity

  private def isFresh(stamp: Instant): Boolean = Instant.now isBefore stamp.plus(expiration.toMillis, MILLIS)

  case class Record(tweets: Seq[Tweet], stamp: Instant = Instant.now)

  private val evictCache = new Runnable { def run(): Unit = {
    store
      .iterator
      .filterNot { case (_, Record(_, stamp)) => isFresh(stamp) }
      .foreach { case (key, Record(_, _)) => store.remove(key) }
  } }
  /** @note see [[https://stackoverflow.com/questions/16625464/scheduled-executor-in-scala]] */
  actorSystem.scheduler.schedule(eviction, eviction, evictCache)
}
