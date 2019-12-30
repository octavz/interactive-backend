/*
package com.wantsome.interactive

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.finch._
import io.finch.circe.dropNullValues._
import zio.{IO => _, _}
import zio.interop.catz._
import zio.blocking.Blocking
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.refined._
import com.github.mlangc.slf4zio.api._

import com.wantsome.common._, db._, data._
import com.wantsome.verifyr.auth._

import dto._
import implicits._

trait FinchEnv extends SettingsProvider with Auth with Repo with TransactorProvider

object mainFinch extends Endpoint.Module[RIO[FinchEnv, *]] with LoggingSupport with DefaultRuntime {
  object liveEnv extends LiveSettingsProvider with LiveAuth with LiveRepo with Blocking.Live

  type AppS[A] = RIO[FinchEnv, A]

  def healthcheck: Endpoint[AppS, String] = get(pathEmpty) {
    ZIO.succeed("ok").map(Ok): AppS[Output[String]]
  }

  def helloWorld: Endpoint[AppS, UserDTO] = get("hello") {
    val rawJson = """
                    |{ "email":"test@exmaple.com", "firstName":"John", "lastName":"Popescu",
                    |"birthday":"1570982164", "city":"Iasi", "phone":"+40742012378", "occupation":"5",
                    |"fieldOfWork":"4", "englishLevel":"3","itExperience":true, "heardFrom":"1" }""".stripMargin
    val user = parse(rawJson).fold(ZIO.fail, _.as[UserDTO].fold(ZIO.fail, ZIO.succeed))
    user.map(Ok): AppS[Output[UserDTO]]
  }

  def combos: Endpoint[AppS, CombosDTO] = get("combos") {
    service.combos.map { dic =>
      CombosDTO(
        englishLevel = dic(EnglishLevel).values,
        occupation = dic(Occupation).values,
        fieldOfWork = dic(FieldOfWork).values)
    }.map(Ok): AppS[Output[CombosDTO]]
  }

  def hello: Endpoint[AppS, String] = get("hello" :: path[String]) { s: String =>
    Task
      .fail(new Exception(s))
      .map(v => Ok(v))
      .catchSome {
        case t: Exception => ZIO.succeed(InternalServerError(t))
      }: AppS[Output[String]]
  }

  private def migrate[R <: SettingsProvider]: RIO[R, Int] =
    settings.config[SettingsProvider] >>= { c =>
      migration.migrate(
        schema = c.database.schema.value,
        jdbcUrl = c.database.url.value,
        user = c.database.user.value,
        password = c.database.password.value)
    }

  def httpService(implicit r: Runtime[FinchEnv]): AppS[Service[Request, Response]] =
    ZIO.effect(
      Bootstrap
        .serve[Text.Plain](healthcheck)
        .serve[Application.Json](helloWorld :+: combos :+: hello)
        .toService)

  def run(args: List[String]) = {
    val managedTransactor =
      settings
        .managedTransactor(platform.executor.asEC)
        .provide(liveEnv)

    val io = for {
      _ <- migrate[SettingsProvider]
      service <- managedTransactor.use { t =>
        ZIO
          .runtime[FinchEnv]
          .flatMap(implicit runtime => httpService)
          .provideSome[SettingsProvider with Repo with Auth] { e =>
            new FinchEnv {
              override val transactor = () => t
              override def userRepo = e.userRepo
              override val auth = e.auth
              override val settingsProvider = e.settingsProvider
            }
          }
      }
      _ <- ZIO.effect(Await.ready(Http.server.serve(":8081", service))) *> ZIO.never
    } yield ()
    io.provide(liveEnv).foldM(t => logger.errorIO("Failed in main", t).as(0), _ => ZIO.succeed(1))
  }
}
 */
