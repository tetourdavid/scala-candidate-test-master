package com.letgo.scala_candidate_test.application

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.MissingQueryParamRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

import com.letgo.scala_candidate_test.Fixtures._
import com.letgo.scala_candidate_test.domain.{Tweet, TweetRepository}
import com.letgo.scala_candidate_test.infrastructure.TweetClient.UserNotFoundException
import com.letgo.scala_candidate_test.infrastructure.TweetMemoryRepository

@RunWith(classOf[JUnitRunner])
class ShoutControllerSpec extends FlatSpec with Matchers with ScalatestRouteTest with MockFactory {

  private implicit val actorSystem: ActorSystem = ActorSystem()
  private val ExistingUser = "existingUsername"
  private val NonExistingUser = "nonExistingUsername"

  private val tweetClient = mock[TweetRepository]
  (tweetClient.searchByUserName _).expects(*, *).anyNumberOfTimes.returns(Future.successful(Tweets))

  private val tweetRepository = new TweetMemoryRepository(tweetClient, LongDuration, LongDuration, Capacity) {
    override def searchByUserName(username: String, limit: Int): Future[Seq[Tweet]] =
      (username, limit) match {
        case (NonExistingUser, _) => throw UserNotFoundException(NonExistingUser)
        case _ => tweetClient.searchByUserName(username, limit)
      }
  }

  private val controller: ShoutController = new ShoutController(tweetRepository, Limit )

  behavior of "Shout controller"

  it should "return 200 for proper request" in {
    Get(s"/shout/$ExistingUser?limit=$Limit") ~> controller.route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "return 400 for missing limit" in {
    Get("/shout/realDonaldTrump") ~> controller.route ~> check {
      rejection shouldEqual MissingQueryParamRejection("limit")
    }
  }

  it should "return 404 for non-existing user" in {
    Get(s"/shout/$NonExistingUser?limit=$Limit") ~> controller.route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "return 422 for more tweets than permitted by limit" in {
    Get(s"/shout/$ExistingUser?limit=${Limit + 1}") ~> controller.route ~> check {
      status shouldEqual StatusCodes.UnprocessableEntity
    }
  }

  it should "return 422 for negative tweet limit" in {
    Get(s"/shout/$ExistingUser?limit=-3") ~> controller.route ~> check {
      status shouldEqual StatusCodes.UnprocessableEntity
    }
  }

}
