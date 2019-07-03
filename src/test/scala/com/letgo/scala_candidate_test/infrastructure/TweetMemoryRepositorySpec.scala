package com.letgo.scala_candidate_test.infrastructure

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.letgo.scala_candidate_test.domain.{Tweet, TweetRepository}
import org.junit.runner.RunWith
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable
import scala.concurrent.duration.Duration

import scala.language.postfixOps

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class TweetMemoryRepositorySpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  import TweetMemoryRepositorySpec._

  private implicit val actorSystem: ActorSystem = ActorSystem()

  behavior of "Tweet memory repository"

  it should "return no cached tweets above limit" in {
    val client = mock[TweetRepository]
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, LongDuration, LongDuration, Capacity) {
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        // 3 tweets are stored
        store.put(CachedName, Record(SampleTweets))
      }
    // 2 tweets are requested
    repository.searchByUserName(CachedName, 2) map assertResult(SampleTweets take 2)
  }

  it should "not call client if tweets are cached" in {
    val client = mock[TweetRepository]
    // test will fail if client gets called
    client.searchByUserName _ expects(*, *) never
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, LongDuration, LongDuration, Capacity) {
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        store.put(CachedName, Record(SampleTweets))
      }
    repository.searchByUserName(CachedName, SampleTweets.size) map assertResult(SampleTweets)
  }

  it should "call client if cache is empty and subsequently use cache" in {
    val client = mock[TweetRepository]
    (client.searchByUserName _ expects(UnCachedName, SampleTweets.size)).once returns Future.successful(SampleTweets)
    val repository = new TweetMemoryRepository(client, LongDuration, LongDuration, Capacity)
    // for comprehension used for sequential future resolution
    for {
      first  <- repository.searchByUserName(UnCachedName, SampleTweets.size)
      second <- repository.searchByUserName(UnCachedName, SampleTweets.size)
      third  <- repository.searchByUserName(UnCachedName, SampleTweets.size)
    } yield {
      first  shouldEqual SampleTweets
      second shouldEqual SampleTweets
      third  shouldEqual SampleTweets
    }
  }

  it should "call client if tweet cache is expired" in {
    val client = mock[TweetRepository]
    (client.searchByUserName _ expects(CachedName, SampleTweets.size)).once returns Future.successful(SampleTweets)
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, TinyDuration, LongDuration, Capacity) {
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        store.put(CachedName, Record(SampleTweets))
      }
    // waiting for the cache to expire
    Thread.sleep(5)
    repository.searchByUserName(CachedName, SampleTweets.size) map assertResult(SampleTweets)
  }

  it should "call client if not enough tweets are cached" in {
    val client = mock[TweetRepository]
    (client.searchByUserName _ expects(CachedName, SampleTweets.size)).once returns Future.successful(SampleTweets)
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, LongDuration, LongDuration, Capacity) {
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        // 2 tweets are be cached
        store.put(CachedName, Record(SampleTweets take 2))
      }
    // 3 tweets are requested
    repository.searchByUserName(CachedName, SampleTweets.size) map assertResult(SampleTweets)
  }

  // todo test caching at full capacity and cache eviction

}

object TweetMemoryRepositorySpec {

  private val SampleTweets = Seq(Tweet("Joining"), Tweet("LetGo,"), Tweet("soon."))
  private val CachedName = "cachedName"
  private val UnCachedName = "unCachedName"

  private val LongDuration = Duration(1, TimeUnit.HOURS)
  private val TinyDuration = Duration(1, TimeUnit.MILLISECONDS)

  private val Capacity = 5
}
