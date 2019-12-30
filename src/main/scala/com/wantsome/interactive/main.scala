package com.wantsome

package interactive

import org.http4s._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._

import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._

import sttp.tapir._
import sttp.tapir.server.http4s._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.http4s.SwaggerHttp4s

import cats.implicits._
import com.github.mlangc.slf4zio.api._

import com.wantsome.common._, db._
import com.wantsome.verifyr.auth._

object main extends zio.App with LoggingSupport {
  import io.circe.generic.auto._
  import sttp.tapir.json.circe._
  trait Env extends SettingsProvider with Auth with Repo with TransactorProvider with Clock
  object liveEnv extends LiveSettingsProvider with LiveAuth with LiveRepo with Blocking.Live

  val port: Int = Option(System.getenv("HTTP_PORT"))
    .map(_.toInt)
    .getOrElse(8080)

  private def migrate[R <: SettingsProvider]: RIO[R, Int] =
    settings.config[R] >>= { c =>
      migration.migrate(
        schema = c.database.schema.value,
        jdbcUrl = c.database.url.value,
        user = c.database.user.value,
        password = c.database.password.value)
    }
  type AppS[A] = RIO[Env, A]

  // extension methods for ZIO; not a strict requirement, but they make working with ZIO much nicer
  implicit class ZioEndpoint[I, E, O](e: Endpoint[I, E, O, EntityBody[AppS]]) {

    def toZioRoutes(logic: I => IO[E, O])(implicit serverOptions: Http4sServerOptions[AppS]): HttpRoutes[AppS] = {
      import sttp.tapir.server.http4s._
      e.toRoutes(i => logic(i).either)
    }

    def zioServerLogic(logic: I => IO[E, O]): ServerEndpoint[I, E, O, EntityBody[AppS], AppS] =
      ServerEndpoint(e, logic(_).either)
  }

  //

  case class Pet(species: String, url: String)

  val petEndpoint: Endpoint[Int, String, Pet, Nothing] =
    endpoint.get.in("pet" / path[Int]("petId")).errorOut(stringBody).out(jsonBody[Pet])

  val service: HttpRoutes[AppS] = petEndpoint.toZioRoutes { petId =>
    if (petId == 35) {
      UIO(Pet("Tapirus terrestris", "https://en.wikipedia.org/wiki/Tapir"))
    } else {
      IO.fail("Unknown pet id")
    }
  }

  // Or, using server logic:

  val petServerEndpoint = petEndpoint.zioServerLogic { petId =>
    if (petId == 35) {
      UIO(Pet("Tapirus terrestris", "https://en.wikipedia.org/wiki/Tapir"))
    } else {
      IO.fail("Unknown pet id")
    }
  }

  import sttp.tapir.docs.openapi._
  import sttp.tapir.openapi.circe.yaml._
  val yaml = List(petEndpoint).toOpenAPI("Our pets", "1.0").toYaml
  val service2: HttpRoutes[AppS] = petServerEndpoint.toRoutes

  def serve(implicit runtime: Runtime[Env]) =
    BlazeServerBuilder[AppS]
      .bindHttp(8080, "localhost")
      .withHttpApp(Router("/" -> (service2 <+> new SwaggerHttp4s(yaml).routes[AppS])).orNotFound)
      .serve
      .compile
      .drain

  override def run(args: List[String]): URIO[ZEnv, Int] = {
    val managedTransactor =
      settings
        .managedTransactor(platform.executor.asEC)
        .provide(liveEnv)

    val io = for {
      _ <- migrate
      service <- managedTransactor.use { t =>
        ZIO
          .runtime[Env]
          .flatMap(implicit runtime => serve)
          .provideSome[SettingsProvider with Repo with Auth] { e =>
            new Env with Clock.Live {
              override val transactor = () => t
              override def userRepo = e.userRepo
              override val auth = e.auth
              override val settingsProvider = e.settingsProvider
            }
          }
      } *> ZIO.never
    } yield service
    io.provide(liveEnv).foldM(t => logger.errorIO("Failed in main", t).as(0), _ => ZIO.succeed(1))
  }
}
