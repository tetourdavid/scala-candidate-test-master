package com.letgo.scala_candidate_test.application

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import com.letgo.scala_candidate_test.application.ShoutController.BadNumberOfTweetsException
import com.letgo.scala_candidate_test.domain.TweetRepository
import com.letgo.scala_candidate_test.infrastructure.TweetClient.UserNotFoundException
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext

class ShoutController(tweetRepository: TweetRepository, limit: Int)(implicit ec: ExecutionContext)
  extends Directives with JsonSupport with StrictLogging {

  private val errorHandler = {
    ExceptionHandler {
      case e: BadNumberOfTweetsException => logger.warn(e.getMessage); complete(UnprocessableEntity, e.getMessage)
      case e: UserNotFoundException      => logger.warn(e.getMessage); complete(NotFound, e.getMessage)
      case e: Throwable                  => logger.error(e.getMessage, e); complete(InternalServerError, e.getMessage)
    }
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
