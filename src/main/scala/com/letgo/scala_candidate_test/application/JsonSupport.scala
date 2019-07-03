package com.letgo.scala_candidate_test.application

import java.nio.charset.StandardCharsets._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import com.letgo.scala_candidate_test.domain.Tweet
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContext, Future}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  import spray.json._

  /** @note matches readme examples on [[https://github.com/spray/spray-json]] */
  implicit object TweetsFormat extends RootJsonFormat[Seq[Tweet]] {
    override def write(obj: Seq[Tweet]): JsValue = {
      JsArray(obj.map(tweet => JsString(encoded(tweet.shoutedText))).toVector)
    }
    override def read(json: JsValue): Seq[Tweet] = {
      JsArray(json).elements.map(text => Tweet(text.toString)).seq
    }
  }

  implicit def marshallTweets(tweets: Future[Seq[Tweet]])
                             (implicit ec: ExecutionContext): ToResponseMarshallable = tweets.map(_.toJson)

  /** @note based on [[https://stackoverflow.com/questions/5729806/encode-string-to-utf-8]] */
  private def encoded(text: String) = new String(text.getBytes(ISO_8859_1), UTF_8)
}