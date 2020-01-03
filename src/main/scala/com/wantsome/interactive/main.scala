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
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.json.circe._

import io.circe.generic.auto._

import cats.implicits._
import com.github.mlangc.slf4zio.api._

import com.wantsome.common._, db._
import com.wantsome.verifyr.auth._
import com.wantsome.interactive.dto.CombosDTO
import com.wantsome.common.data._
import com.wantsome.interactive.dto._

object main extends zio.App with LoggingSupport with TapirJsonCirce {
  type Env = AuthProvider with Clock

  // val liveEnv = new AuthProvider with Clock.Live {
  //   override val repo = (new verifyr.auth.LiveRepo{}).userRepo
  // }
  //
  val settingsProvider = new LiveSettingsProvider {}

  def liveEnv(t: doobie.Transactor[Task]) = new LiveAuthProvider with Clock.Live {
    override val repo = new LiveRepo {
      override val transactorProvider = new TransactorProvider {
        override val transactor = t
      }
    }.userRepo
  }

  type AppS[A] = RIO[Env, A]

  val port: Int = Option(System.getenv("HTTP_PORT"))
    .map(_.toInt)
    .getOrElse(8080)

  private def migrate: RIO[Any, Int] =
    settingsProvider.zioConfig >>= { c =>
      migration.migrate(
        schema = c.database.schema.value,
        jdbcUrl = c.database.url.value,
        user = c.database.user.value,
        password = c.database.password.value)
    }

  implicit class ZioEndpoint[I, E, O](e: Endpoint[I, E, O, EntityBody[AppS]]) {

    def toZioRoutes(logic: I => ZIO[Env, E, O])(
      implicit serverOptions: Http4sServerOptions[AppS]): HttpRoutes[AppS] = {
      e.toRoutes(i => logic(i).either)
    }

    def zioServerLogic(logic: I => IO[E, O]): ServerEndpoint[I, E, O, EntityBody[AppS], AppS] =
      ServerEndpoint(e, logic(_).either)
  }

  case class Pet(species: String, url: String)

  val petEndpoint: Endpoint[Int, String, Pet, Nothing] =
    endpoint.get.in("pet" / path[Int]("petId")).errorOut(stringBody).out(jsonBody[Pet])

  val combosEndpoint: Endpoint[Unit, String, CombosDTO, Nothing] =
    endpoint.get
      .in("combos")
      .errorOut(stringBody)
      .out(jsonBody[CombosDTO])

  val combos = combosEndpoint.toZioRoutes { _ =>
    AuthProvider.>.combos
      .map(dic =>
        CombosDTO(
          englishLevel = dic(EnglishLevel).values.map(ComboValueDTO(_)),
          occupation = dic(Occupation).values.map(ComboValueDTO(_)),
          fieldOfWork = dic(FieldOfWork).values.map(ComboValueDTO(_))
        ))
      .mapError(_.getMessage)
  }

  val pet = petEndpoint.toZioRoutes { petId =>
    if (petId == 35) {
      UIO(Pet("Tapirus terrestris", "https://en.wikipedia.org/wiki/Tapir"))
    } else {
      IO.fail("Unknown pet id")
    }
  }

  val yaml = List(petEndpoint, combosEndpoint).toOpenAPI("Registration API", "1.0").toYaml

  def serve(implicit runtime: Runtime[Env]) =
    BlazeServerBuilder[AppS]
      .bindHttp(8080, "localhost")
      .withHttpApp(
        Router("/" -> (combos <+> pet <+> new SwaggerHttp4s(yaml).routes[AppS])).orNotFound)
      .serve
      .compile
      .drain

  override def run(args: List[String]): URIO[ZEnv, Int] = {
    val managedTransactor =
      settings
        .managedTransactor(settingsProvider, platform.executor.asEC)
        .provide(new Blocking.Live {})

    val io = for {
      _ <- migrate
      service <- managedTransactor.use { t =>
        ZIO
          .runtime[Env]
          .flatMap(implicit runtime => serve)
          .provide(liveEnv(t))
      } *> ZIO.never
    } yield service
    io.foldM(t => logger.errorIO("Failed in main", t).as(0), _ => ZIO.succeed(1))
  }
}
