package com.letgo.scala_candidate_test.infrastructure

import akka.actor.ActorSystem
import com.letgo.scala_candidate_test.domain.TweetRepository
import org.junit.runner.RunWith
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner
import com.letgo.scala_candidate_test.Fixtures._

import scala.collection.mutable

import scala.language.postfixOps

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class TweetMemoryRepositorySpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  private implicit val actorSystem: ActorSystem = ActorSystem()
  private val CachedName = "cachedName"
  private val UnCachedName = "unCachedName"

  behavior of "Tweet memory repository"

  it should "return no cached tweets above limit" in {
    val client = mock[TweetRepository]
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, LongDuration, LongDuration, Capacity) {
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        // 3 tweets are stored
        store.put(CachedName, Record(Tweets))
      }
    // 2 tweets are requested
    repository.searchByUserName(CachedName, 2) map assertResult(Tweets take 2)
  }

  it should "not call client if tweets are cached" in {
    val client = mock[TweetRepository]
    // test will fail if client gets called
    client.searchByUserName _ expects(*, *) never
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, LongDuration, LongDuration, Capacity) {
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        store.put(CachedName, Record(Tweets))
      }
    repository.searchByUserName(CachedName, Tweets.size) map assertResult(Tweets)
  }

  it should "call client if cache is empty and subsequently use cache" in {
    val client = mock[TweetRepository]
    (client.searchByUserName _ expects(UnCachedName, Tweets.size)).once returns Future.successful(Tweets)
    val repository = new TweetMemoryRepository(client, LongDuration, LongDuration, Capacity)
    // for comprehension used for sequential future resolution
    for {
      first  <- repository.searchByUserName(UnCachedName, Tweets.size)
      second <- repository.searchByUserName(UnCachedName, Tweets.size)
      third  <- repository.searchByUserName(UnCachedName, Tweets.size)
    } yield {
      first  shouldEqual Tweets
      second shouldEqual Tweets
      third  shouldEqual Tweets
    }
  }

  it should "call client if tweet cache is expired" in {
    val client = mock[TweetRepository]
    (client.searchByUserName _ expects(CachedName, Tweets.size)).once returns Future.successful(Tweets)
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, TinyDuration, LongDuration, Capacity) {
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        store.put(CachedName, Record(Tweets))
      }
    // waiting for the cache to expire
    Thread.sleep(5)
    repository.searchByUserName(CachedName, Tweets.size) map assertResult(Tweets)
  }

  it should "call client if not enough tweets are cached" in {
    val client = mock[TweetRepository]
    (client.searchByUserName _ expects(CachedName, Tweets.size)).once returns Future.successful(Tweets)
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, LongDuration, LongDuration, Capacity) {
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        // 2 tweets are be cached
        store.put(CachedName, Record(Tweets take 2))
      }
    // 3 tweets are requested
    repository.searchByUserName(CachedName, Tweets.size) map assertResult(Tweets)
  }

  // todo test caching at full capacity and cache eviction

}

