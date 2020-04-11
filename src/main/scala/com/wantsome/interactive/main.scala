package com.wantsome.interactive

import org.http4s._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import org.http4s.dsl.io._
import zio._
import zio.logging._
import zio.logging.slf4j._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import sttp.tapir.{auth => _, _}
import sttp.tapir.server.http4s._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import cats.implicits._
import cats.effect.Blocker

import com.wantsome.common.config.SettingsProvider
import com.wantsome.common.migration
import com.wantsome.common.db._
import com.wantsome.common.data._
import com.wantsome.common.dates

import com.wantsome.verifyr.store._
import com.wantsome.verifyr.register._
import com.wantsome.verifyr.sql._

import com.wantsome.interactive.dto.CombosDTO
import com.wantsome.interactive.dto._

object main extends zio.App with TapirJsonCirce {
  val logFormat = "[correlation-id = %s] %s"

  val logL = Slf4jLogger.make { (context, message) =>
    val correlationId = LogAnnotation.CorrelationId.render(
      context.get(LogAnnotation.CorrelationId)
    )
    logFormat.format(correlationId, message)
  }

  def buildDepGraph(t: doobie.Transactor[Task]): ULayer[AuthProvider] = {
    val rl = (TransactorProvider.live(t) ++ StoreBackend.live) >>> Repo.live
    (rl ++ dates.Dates.live ++ SettingsProvider.live) >>> AuthProvider.live
  }

  type AppS[A] = RIO[AuthProvider with Clock with Blocking, A]

  val port: Int = Option(System.getenv("HTTP_PORT"))
    .map(_.toInt)
    .getOrElse(5080)

  implicit class ZioEndpoint[I, E, O](e: Endpoint[I, E, O, EntityBody[AppS]]) {

    def toZioRoutes(
        logic: I => ZIO[AuthProvider with Clock, E, O]
    )(implicit serverOptions: Http4sServerOptions[AppS]): HttpRoutes[AppS] = {
      e.toRoutes(i => logic(i).either)
    }

    def zioServerLogic(
        logic: I => IO[E, O]
    ): ServerEndpoint[I, E, O, EntityBody[AppS], AppS] =
      ServerEndpoint(e, logic(_).either)
  }

  val combosEndpoint: Endpoint[Unit, String, CombosDTO, Nothing] =
    endpoint.get
      .in("combos")
      .errorOut(stringBody)
      .out(jsonBody[CombosDTO])

  val combos = combosEndpoint.toZioRoutes { _ =>
    AuthProvider.combos
      .map(dic =>
        CombosDTO(
          englishLevel = dic(EnglishLevel).values.map(ComboValueDTO(_)),
          occupation = dic(Occupation).values.map(ComboValueDTO(_)),
          fieldOfWork = dic(FieldOfWork).values.map(ComboValueDTO(_))
        )
      )
      .mapError(_.getMessage)
  }

  val yaml = List(combosEndpoint).toOpenAPI("Registration API", "1.0").toYaml

  def serve(implicit r: Runtime[AuthProvider with Clock with Blocking]) =
    BlazeServerBuilder[AppS]
      .bindHttp(8080, "localhost")
      .withHttpApp(
        Router(
          "/" -> (combos <+> new SwaggerHttp4s(yaml)
            .routes[AppS])
        ).orNotFound
      )
      .serve
      .compile
      .drain

  val zl: ZLayer[Clock, Nothing, Clock with Blocking] =
    ZLayer.requires[Clock] ++ Blocking.live
  override def run(args: List[String]): URIO[ZEnv, Int] = {
    val io = for {
      _ <- migration.migrate
      c <- SettingsProvider.config
      ec = platform.executor.asEC
      blockEC <- ZIO.access[Blocking](_.get.blockingExecutor.asEC)
      managedTransactor = mkTransactor(c.database, ec, blockEC)
      service <- managedTransactor.use { t =>
        (ZIO
          .runtime[AuthProvider with Clock with Blocking]
          .flatMap(serve(_)))
          .provideLayer(buildDepGraph(t) ++ Blocking.live ++ Clock.live)
      } *> ZIO.never
    } yield service
    io.provideLayer(
        SettingsProvider.live ++ Blocking.live ++ Clock.live ++ logL
      )
      .foldM(
        t => log.throwable("Failed in main", t).provideLayer(logL).as(0),
        _ => ZIO.succeed(1)
      )
  }
}
