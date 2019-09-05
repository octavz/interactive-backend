package com.wantsome.verifyr

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import zio._
import io.finch._
import zio.interop.catz._
//import io.finch.catsEffect._
import io.finch.circe._
import io.circe.generic.auto._

object Main extends zio.App with EndpointModule[Task]{

  case class Message(hello: String)
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

  Await.ready(Http.server.serve(":8081", service))

  override def run(args: List[String]): ZIO[Main.Environment, Nothing, Int] =
    ZIO.effectAsync[Any,Throwable, Unit](cb => )
}
