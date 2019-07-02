package com.letgo.scala_candidate_test.domain

import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TweetSpec extends FlatSpec with Matchers {

  behavior of "shoutText"

  it should "convert to uppercase and add exclamation mark" in {
    Tweet("Sample un-punctuated 38 character line").shoutedText shouldEqual
      "SAMPLE UN-PUNCTUATED 38 CHARACTER LINE!"
  }

  it should "convert to uppercase and replace punctuation with an exclamation mark" in {
    Tweet("Sample un-punctuated 39 character line,").shoutedText shouldEqual
      "SAMPLE UN-PUNCTUATED 39 CHARACTER LINE!"
  }

}
