package com.letgo.scala_candidate_test.application

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.server.Directives.{complete, get, handleExceptions, path, _}

import com.letgo.scala_candidate_test.application.ShoutController.BadNumberOfTweetsException

class ShoutController {

  val Limit = 10 // load from configuration

  private val errorHandler = ExceptionHandler {
    case e: BadNumberOfTweetsException => complete(StatusCodes.BadRequest, e.getMessage)
    case _ => complete("error happened")
  }

  val route: Route = handleExceptions(errorHandler) {
    get {
      path("shout" / Segment) { twitterUserName =>
        parameters('limit.as[Int]) { limit =>
            validate(limit)
            complete(s"HELLO ${twitterUserName.toUpperCase()} !")
        }
      }
    }
  }

  /** @throws BadNumberOfTweetsException for too large or negative tweetLimit */
  private def validate(tweetLimit: Int): Unit = {
    if (tweetLimit > Limit || tweetLimit < 0) throw BadNumberOfTweetsException(tweetLimit)
  }
}

object ShoutController extends ShoutController {

  case class BadNumberOfTweetsException(number: Int)
    extends Exception(s"Requested $number tweets, only 0 to $Limit can be requested.")
}
