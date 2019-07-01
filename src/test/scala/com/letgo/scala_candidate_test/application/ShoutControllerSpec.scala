package com.letgo.scala_candidate_test.application

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.MissingQueryParamRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.letgo.scala_candidate_test.domain.TweetRepository
import com.letgo.scala_candidate_test.infrastructure.TweetRepositoryInMemory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ShoutControllerSpec extends FlatSpec with Matchers with ScalatestRouteTest {

  val limit = 10
  val tweetRepository: TweetRepository = new TweetRepositoryInMemory()
  val controller: ShoutController = new ShoutController(tweetRepository, limit)

  behavior of "Shout controller"

  it should "return 400 for missing limit" in {
    Get("/shout/realDonaldTrump") ~> controller.route ~> check {
      rejection shouldEqual MissingQueryParamRejection("limit")
    }
  }

  it should "return 422 for more tweets than permitted by limit" in {
    Get(s"/shout/realDonaldTrump?limit=${limit + 1}") ~> controller.route ~> check {
      status shouldEqual StatusCodes.UnprocessableEntity
    }
  }

  it should "return 422 for negative tweet limit" in {
    Get("/shout/realDonaldTrump?limit=-3") ~> controller.route ~> check {
      status shouldEqual StatusCodes.UnprocessableEntity
    }
  }

}
