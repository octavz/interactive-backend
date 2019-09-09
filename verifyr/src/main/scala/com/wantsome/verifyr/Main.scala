package com.wantsome

package verifyr

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import com.wantsome.commons.models.User
import io.finch._
import zio.interop.catz._
import zio._
import commons.finch.JsonIter._

object Registrator {
  trait Env

  trait Service {
    def registerUser(): RIO[Env, User]
  }
}

object Main extends zio.App with EndpointModule[Task] {
  case class Message(hello: String)
  implicit val codec: JsonValueCodec[Message] = JsonCodecMaker.make[Message](CodecMakerConfig())
  implicit val runtime = this

  def healthcheck: Endpoint[Task, String] = get(pathEmpty) {
    Ok("OK")
  }

  def helloWorld: Endpoint[Task, Message] = get("hello") {
    Ok(Message("World"))
  }

  def hello: Endpoint[Task, Message] = get("hello" :: path[String]) { s: String =>
    Ok(Message(s))
  }

  def service: Service[Request, Response] =
    Bootstrap
      .serve[Text.Plain](healthcheck)
      .serve[Application.Json](helloWorld :+: hello)
      .toService

  override def run(args: List[String]): ZIO[Main.Environment, Nothing, Int] =
    ZIO.effectTotal(Await.ready(Http.server.serve(":8081", service))) *> ZIO.never

}
