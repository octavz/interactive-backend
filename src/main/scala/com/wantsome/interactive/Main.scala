package com.wantsome

package interactive

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import com.wantsome.commons.config.{LiveSettingsProvider, SettingsProvider}
import com.wantsome.commons.db.TransactorProvider
import com.wantsome.commons.logger.LiveLogger
import com.wantsome.verifyr.auth.models.{EnglishLevel, FieldOfWork, Occupation}
import io.finch._
import io.finch.circe.dropNullValues._
import zio._
import zio.interop.catz._
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.refined._
import models._
import commons.logger._
import doobie.util.transactor
import doobie.util.transactor.Transactor
import verifyr.auth._
import zio.blocking.Blocking

object Main extends App with EndpointModule[Task] {
  import json._
  implicit val runtime = new DefaultRuntime {}

  trait Env extends SettingsProvider with LiveAuth with LiveRepo with LiveLogger with TransactorProvider

  def healthcheck: Endpoint[Task, String] = get(pathEmpty) {
    ZIO.succeed("ok").map(Ok): Task[Output[String]]
  }

  def helloWorld: Endpoint[Task, UserDTO] = get("hello") {
    val rawJson = """
                    |{ "email":"test@exmaple.com", "firstName":"John", "lastName":"Popescu",
                    |"birthday":"1980/31/21", "city":"Iasi", "phone":"+40742012378", "occupation":5,
                    |"fieldOfWork":4, "englishLevel":3,"itExperience":2, "heardFrom":1 }""".stripMargin
    val user = parse(rawJson).fold(ZIO.fail, _.as[UserDTO].fold(ZIO.fail, ZIO.succeed))
    user.map(Ok): Task[Output[UserDTO]]
  }

  def combos(env: Env): Endpoint[Task, CombosDTO] = get("combos") {
    val res: RIO[Env, CombosDTO] = service.combos.map { dic =>
      CombosDTO(
        englishLevel = dic(EnglishLevel).values,
        occupation = dic(Occupation).values,
        fieldOfWork = dic(FieldOfWork).values)
    }
    res.provide(env).map(Ok): Task[Output[CombosDTO]]
  }

  /*
  def hello: Endpoint[IO, Message] = get("hello" :: path[String]) { s: String =>
    Ok(Message(s))
  }
   */

  def httpService: Task[Service[Request, Response]] = {
    val s = new LiveSettingsProvider {}
    s.settingsProvider
      .transactor(Platform.executor.asEC)
      .use { xa =>
        val env = new Env {
          override val transactor: TransactorProvider.Service = new TransactorProvider.Service {
            override def apply(): Transactor[Task] = xa
          }
          override val settingsProvider: SettingsProvider.Service = s.settingsProvider
        }

        ZIO.succeed(
          Bootstrap
            .serve[Text.Plain](healthcheck)
            .serve[Application.Json]( /*helloWorld :+: */ combos(env))
            .serve[Application.Json](helloWorld)
            .toService)
      }
      .provide(Blocking.Live)
  }

  override def run(args: List[String]): URIO[Main.Environment, Int] =
    httpService.flatMap { s =>
      ZIO.effectTotal(Await.ready(Http.server.serve(":8081", s))) *> ZIO.never
    }.catchAll { t =>
      error(t)("Failed in main").provide(LiveLogger).as(0)
    }
}
