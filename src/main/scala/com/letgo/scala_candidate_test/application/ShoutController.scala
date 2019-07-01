package com.letgo.scala_candidate_test.application

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.server.Directives.{complete, get, handleExceptions, path, _}

import com.letgo.scala_candidate_test.application.ShoutController.BadNumberOfTweetsException
import com.letgo.scala_candidate_test.domain.TweetRepository

import scala.concurrent.ExecutionContext

class ShoutController(tweetRepository: TweetRepository, limit: Int)
                     (implicit ec: ExecutionContext) {

  private val errorHandler = ExceptionHandler {
    case e: BadNumberOfTweetsException => complete(StatusCodes.UnprocessableEntity, e.getMessage)
    case _ => complete("error happened")
  }

  val route: Route = handleExceptions(errorHandler) {
    get {
      path("shout" / Segment) { twitterUserName =>
        parameters('limit.as[Int]) { count =>
            validate(count)
            complete(tweetRepository.searchByUserName(twitterUserName, count).map(_.map(_.text).mkString("\n")))
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
