package com.letgo.scala_candidate_test.domain

import com.typesafe.config.Config
import scala.language.implicitConversions

import scala.concurrent.duration.{Duration, FiniteDuration}

class ApplicationConfig(root: Config) {

  object binding {
    val interface: String = root.getString("binding.interface")
    val port: Int         = root.getInt("binding.port")
  }

  object api {
    val limit: Int = root.getInt("api.limit")
  }

  object cache {
    val expiration: FiniteDuration = root.getDuration("cache.expiration")
    val eviction: FiniteDuration   = root.getDuration("cache.eviction")
    val capacity: Int              = root.getInt("cache.capacity")
  }

  /** @note see [[https://stackoverflow.com/questions/32076311/converting-java-to-scala-durations]] */
  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration = Duration.fromNanos(d.toNanos)
}

object ApplicationConfig {

  val Basename: String = "tweet-shouter"

}
