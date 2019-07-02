package com.letgo.scala_candidate_test.application

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, Route, Directives}

import com.letgo.scala_candidate_test.application.ShoutController.BadNumberOfTweetsException
import com.letgo.scala_candidate_test.domain.{Tweet, TweetRepository}

import scala.concurrent.{ExecutionContext, Future}

class ShoutController(tweetRepository: TweetRepository, limit: Int)
                     (implicit ec: ExecutionContext) extends Directives with JsonSupport {

  private val errorHandler = ExceptionHandler {
    case e: BadNumberOfTweetsException => complete(StatusCodes.UnprocessableEntity, e.getMessage)
    case _ => complete("error happened")
  }

  val route: Route = handleExceptions(errorHandler) {
    get {
      path("shout" / Segment) { twitterUserName =>
        parameters('limit.as[Int]) { count =>
          validate(count)
          complete(tweetRepository.searchByUserName(twitterUserName, count))
        }
      }
    }
  }

  /** @throws BadNumberOfTweetsException for too large or negative tweetLimit */
  private def validate(tweetCount: Int): Unit = {
    if (tweetCount > limit || tweetCount < 0) throw BadNumberOfTweetsException(tweetCount, limit)
  }
}

object ShoutController {

  case class BadNumberOfTweetsException(requested: Int, limit: Int)
    extends Exception(s"Requested $requested tweets, only 0 to $limit can be requested.")

}
