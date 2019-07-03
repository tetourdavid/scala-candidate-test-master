package com.letgo.scala_candidate_test

import java.util.concurrent.{Executors, TimeUnit}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.letgo.scala_candidate_test.application.ShoutController
import com.letgo.scala_candidate_test.infrastructure.{TweetClient, TweetMemoryRepository}
import com.typesafe.scalalogging.StrictLogging

object Starter extends StrictLogging {
  def main(args: Array[String]): Unit = {

    implicit val actorSystem: ActorSystem        = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

    implicit val context: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

    // todo read from typesafe config
    val limit = 10
    val expiration = Duration(30, TimeUnit.SECONDS)
    val eviction = Duration(30, TimeUnit.SECONDS)
    val capacity = 100000

    val interface = "0.0.0.0"
    val port = 9000

    val tweetClient = new TweetClient()
    val tweetRepository = new TweetMemoryRepository(tweetClient, expiration, eviction, capacity)
    val shoutController = new ShoutController(tweetRepository, limit)

    Await.result(Http().bindAndHandle(shoutController.route, interface, port), Duration.Inf)
    logger.info(s"Tweet shouter bound to $interface:$port.")
  }
}
