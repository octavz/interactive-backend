package com.wantsome.testr

object Quzz {}

/*
import cats.effect.IO
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.finch.catsEffect.{get, path, pathEmpty}
import io.finch._


object Main extends App {
  import commons.finch.JsonIter._
  implicit val codec: JsonValueCodec[Message] = JsonCodecMaker.make[Message](CodecMakerConfig())

  case class Message(hello: String)

  def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok("OK")
  }

  def helloWorld: Endpoint[IO, Message] = get("hello") {
    Ok(Message("World"))
  }

  def hello: Endpoint[IO, Message] = get("hello" :: path[String]) { s: String =>
    Ok(Message(s))
  }

  def service: Service[Request, Response] = Bootstrap
    .serve[Text.Plain](healthcheck)
    .serve[Application.Json](helloWorld :+: hello)
    .toService

  Await.ready(Http.server.serve(":8081", service))
}
 */
