package com.letgo.scala_candidate_test.domain

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.implicitConversions

import com.typesafe.config.Config

class ApplicationConfig(root: Config) {

  object Binding {
    val Interface: String = root.getString("binding.interface")
    val Port: Int         = root.getInt("binding.port")
  }

  object Api {
    val Limit: Int = root.getInt("api.limit")
  }

  object Cache {
    val Expiration: FiniteDuration = root.getDuration("cache.expiration")
    val Eviction: FiniteDuration   = root.getDuration("cache.eviction")
    val Capacity: Int              = root.getInt("cache.capacity")
  }

  /** @note see [[https://stackoverflow.com/questions/32076311/converting-java-to-scala-durations]] */
  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration = Duration.fromNanos(d.toNanos)
}

object ApplicationConfig {

  val Basename: String = "tweet-shouter"

}
