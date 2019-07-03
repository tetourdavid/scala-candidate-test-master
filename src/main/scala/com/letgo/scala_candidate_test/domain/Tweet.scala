package com.letgo.scala_candidate_test.domain

import java.util.regex.Pattern

case class Tweet(text: String) {

  /** @return text in uppercase with exclamation mark at the end */
  def shoutedText: String = {
    val shout = text.toUpperCase
    if (Pattern.matches("\\p{Punct}", text takeRight 1)) {
      shout.substring(0, shout.length - 1) + "!"
    } else { shout + "!" }
  }
}
