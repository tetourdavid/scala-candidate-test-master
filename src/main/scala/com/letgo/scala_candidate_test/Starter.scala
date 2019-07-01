package com.letgo.scala_candidate_test

import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import com.letgo.scala_candidate_test.application.ShoutController
import com.letgo.scala_candidate_test.infrastructure.TweetRepositoryInMemory

object Starter {
  def main(args: Array[String]): Unit = {

    implicit val actorSystem: ActorSystem        = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

    implicit val context: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

    val tweetRepository = new TweetRepositoryInMemory()
    val shoutController = new ShoutController(tweetRepository, 10)

    Await.result(Http().bindAndHandle(shoutController.route, "0.0.0.0", 9000), Duration.Inf)
  }
}
