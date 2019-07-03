package com.letgo.scala_candidate_test

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{Duration, FiniteDuration}

import com.letgo.scala_candidate_test.domain.Tweet

object Fixtures {

  val Tweets: Seq[Tweet] = Seq(Tweet("Joining"), Tweet("LetGo,"), Tweet("soon."))

  val LongDuration: FiniteDuration = Duration(1, TimeUnit.HOURS)
  val TinyDuration: FiniteDuration = Duration(1, TimeUnit.MILLISECONDS)

  val Capacity: Int = 5
  val Limit: Int = 10
}
