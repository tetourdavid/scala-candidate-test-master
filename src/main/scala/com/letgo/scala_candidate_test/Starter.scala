package com.letgo.scala_candidate_test

import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.letgo.scala_candidate_test.application.ShoutController
import com.letgo.scala_candidate_test.domain.ApplicationConfig
import com.letgo.scala_candidate_test.infrastructure.{TweetClient, TweetMemoryRepository}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

object Starter extends StrictLogging {
  def main(args: Array[String]): Unit = {

    implicit val actorSystem: ActorSystem        = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

    implicit val context: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

    val config = new ApplicationConfig(ConfigFactory.load().getConfig(ApplicationConfig.Basename))

    val interface  = config.binding.interface
    val port       = config.binding.port
    val limit      = config.api.limit
    val expiration = config.cache.expiration
    val eviction   = config.cache.eviction
    val capacity   = config.cache.capacity

    val tweetClient     = new TweetClient()
    val tweetRepository = new TweetMemoryRepository(tweetClient, expiration, eviction, capacity)
    val shoutController = new ShoutController(tweetRepository, limit)

    Await.result(Http().bindAndHandle(shoutController.route, interface, port), Duration.Inf)
    logger.info(s"Tweet shouter bound to $interface:$port.")
  }
}
