package com.letgo.scala_candidate_test.infrastructure

import java.time.Instant

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
import scala.util.Success

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

  it should "not cache over capacity" in {
    val client = mock[TweetRepository]
    (client.searchByUserName _ expects(UnCachedName, Tweets.size)).twice returns Future.successful(Tweets)
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, LongDuration, LongDuration, Capacity) {
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        // fills cache to capacity
        (1 to Capacity).map(index => store.put(CachedName + index, Record(Tweets)))
      }
    // both requests should rely on client with cache at full capacity
    for {
      first <- repository.searchByUserName(UnCachedName, Tweets.size)
      second <- repository.searchByUserName(UnCachedName, Tweets.size)
    } yield {
      first shouldEqual Tweets
      second shouldEqual Tweets
    }
  }

  it should "evict expired tweets from cache" in {
    // cache will be filled to capacity, all even records will be expired
    val expiredCount = Capacity / 2
    val client = mock[TweetRepository]
    (client.searchByUserName _ expects(*, Tweets.size)).repeated(expiredCount) returns Future.successful(Tweets)
    val repository: TweetMemoryRepository =
      new TweetMemoryRepository(client, LongDuration, TinyDuration, Capacity){
        override val store: mutable.HashMap[String, Record] = mutable.HashMap()
        // fills cache to capacity
        (1 to Capacity) map (index =>
          store.put(CachedName + index, Record(Tweets, if (index % 2 == 0) Instant.MIN else Instant.now)))
      }
    // waiting to evict expired records
    Thread.sleep(5)
    val futures = (1 to Capacity) map (index => repository.searchByUserName(CachedName + index, Tweets.size))
    val future = Future.sequence(futures.map(_.transform(Success(_)))).map(_.collect { case Success(x) => x })
    future.map(allTweets => assert(allTweets.forall(_ == Tweets)))
  }

}

