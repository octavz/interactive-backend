package com.wantsome

package interactive

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.finch._
import io.finch.circe.dropNullValues._
import io.circe.syntax._
import io.circe.parser._
import zio._
import zio.interop.catz._

import commons.models._
import io.circe._, generic.auto._, refined._

object Main extends App with EndpointModule[Task] {
  implicit val runtime = new DefaultRuntime {}

  def healthcheck: Endpoint[Task, String] = get(pathEmpty) {
    ZIO.succeed("ok").map(Ok): Task[Output[String]]
  }

  def helloWorld: Endpoint[Task, User] = get("hello") {
    val rawJson = """
                    |{ "email":"test@exmaple.com", "firstName":"John", "lastName":"Popescu",
                    |"birthday":"1980/31/21", "city":"Iasi", "phone":"+40742012378", "occupation":5,
                    |"fieldOfWork":4, "englishLevel":3,"itExperience":2, "heardFrom":1 }""".stripMargin
    val user = parse(rawJson).fold(ZIO.fail, _.as[User].fold(ZIO.fail, ZIO.succeed))
    user.map(Ok): Task[Output[User]]
  }

  /*
  def hello: Endpoint[IO, Message] = get("hello" :: path[String]) { s: String =>
    Ok(Message(s))
  }
   */

  def service: Service[Request, Response] =
    Bootstrap
      .serve[Text.Plain](healthcheck)
      .serve[Application.Json](helloWorld /*:+: hello*/ )
      .toService

  override def run(args: List[String]): ZIO[Main.Environment, Nothing, Int] =
    ZIO.effectTotal(Await.ready(Http.server.serve(":8081", service))) *> ZIO.never
}
