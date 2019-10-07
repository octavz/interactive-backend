package com.wantsome

package interactive

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.finch._
import io.finch.circe.dropNullValues._
import cats.effect.ConcurrentEffect
import cats.effect.IO
import zio.{IO => _, _}
import zio.interop.catz._
import zio.blocking.Blocking
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.refined._
import doobie.util.transactor.Transactor
import com.github.mlangc.slf4zio.api._
import commons.config.{AppConfig, LiveSettingsProvider, SettingsProvider}
import commons.db.{migration, TransactorProvider}
import verifyr.auth._
import verifyr.auth.models.{EnglishLevel, FieldOfWork, Occupation}
import models._
import json._

object Main extends zio.App with Endpoint.Module[cats.effect.IO] with LoggingSupport {
  implicit val runtime: DefaultRuntime = new DefaultRuntime {}

  trait Env extends SettingsProvider with LiveAuth with LiveRepo with TransactorProvider

  implicit class ToIO[A](val zio: Task[A]) extends AnyVal {
    def toIO: IO[A] = ConcurrentEffect.toIOFromRunCancelable(zio)
  }

  def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    ZIO.succeed("ok").map(Ok).toIO
  }

  def helloWorld: Endpoint[IO, UserDTO] = get("hello") {
    val rawJson = """
                    |{ "email":"test@exmaple.com", "firstName":"John", "lastName":"Popescu",
                    |"birthday":"1980/31/21", "city":"Iasi", "phone":"+40742012378", "occupation":5,
                    |"fieldOfWork":4, "englishLevel":3,"itExperience":2, "heardFrom":1 }""".stripMargin
    val user = parse(rawJson).fold(ZIO.fail, _.as[UserDTO].fold(ZIO.fail, ZIO.succeed))
    user.map(Ok).toIO
  }

  def combos(env: Env): Endpoint[IO, CombosDTO] = get("combos") {
    service.combos.map { dic =>
      CombosDTO(
        englishLevel = dic(EnglishLevel).values,
        occupation = dic(Occupation).values,
        fieldOfWork = dic(FieldOfWork).values)
    }.provide(env).map(Ok).toIO
  }

  def hello: Endpoint[IO, String] = get("hello" :: path[String]) { s: String =>
    Task.succeed(Ok(s)).toIO
  }

  private lazy val s = new LiveSettingsProvider {}

  private def migrate: Task[Int] =
    s.settingsProvider.config >>= { c =>
      migration.migrate(
        schema = c.database.schema.value,
        jdbcUrl = c.database.url.value,
        user = c.database.user.value,
        password = c.database.password.value)
    }

  def httpService: Task[Service[Request, Response]] =
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
            .serve[Application.Json](helloWorld :+: hello)
            .toService)
      }
      .provide(Blocking.Live)

  override def run(args: List[String]): URIO[Main.Environment, Int] = {
    val io = for {
      service <- httpService
      _ <- migrate
      ret <- ZIO.effect(Await.ready(Http.server.serve(":8081", service))) *> ZIO.never
    } yield ret
    io.catchAll(t => logger.errorIO("Failed in main", t).as(0))
  }

}
