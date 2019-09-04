package com.wantsome.interactive

import scala.collection.JavaConverters._
import scala.util.Try
import scala.concurrent._
import cats.effect.ExitCode
import com.wantsome.interactive.model.config.{Application, Database}
import com.wantsome.interactive.module.db.{LiveUserRepository, UserRepository}
import com.wantsome.interactive.module.logger.{LiveLogger, Logger => MyLogger}
import com.wantsome.interactive.route.UserRoute
import com.typesafe.config.{Config, ConfigFactory}
import eu.timepit.refined.auto._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import tapir.docs.openapi._
import tapir.openapi.circe.yaml._
import tapir.swagger.http4s.SwaggerHttp4s
import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.interop.catz._
import zio._
import doobie._
import doobie.hikari._

object Main extends App {
  type AppEnvironment = Clock with Console with UserRepository with MyLogger
  private val userRoute = new UserRoute[AppEnvironment]
  private val yaml = userRoute.getEndPoints.toOpenAPI("User", "1.0").toYaml

  def mkTransactor(config: Database,
                   connectEC: ExecutionContext,
                   transactEC: ExecutionContext
                  ): Managed[Throwable, Transactor[Task]] = {
    val xa = HikariTransactor.newHikariTransactor[Task](
      config.className, config.url, config.user, config.password, connectEC, cats.effect.Blocker.liftExecutionContext(transactEC))
      .map { r =>
        r.kernel.setSchema(config.schema)
        r
      }
    val res = xa.allocated.map {
      case (transactor, cleanupM) =>
        Reservation(ZIO.succeed(transactor), _ => cleanupM.orDie)
    }.uninterruptible
    ZManaged(res)
  }

  override def run(args: List[String]): ZIO[Main.Environment, Nothing, Int] = {
    val result = for {
      application <- ZIO.fromTry(Try(Application.getConfig))

      httpApp = Router("/" -> userRoute.getRoutes, "/docs" -> new SwaggerHttp4s(yaml).routes[TaskR[AppEnvironment, ?]]).orNotFound
      finalHttpApp = Logger.httpApp[ZIO[AppEnvironment, Throwable, ?]](true, true)(httpApp)

      server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
        BlazeServerBuilder[ZIO[AppEnvironment, Throwable, ?]]
          .bindHttp(application.server.port, application.server.host.getHostAddress)
          .withHttpApp(finalHttpApp)
          .serve
          .compile[ZIO[AppEnvironment, Throwable, ?], ZIO[AppEnvironment, Throwable, ?], ExitCode]
          .drain
      }

      blockingEC <- Environment.blocking.blockingExecutor.map(_.asEC)
      resXa = mkTransactor(application.database, Platform.executor.asEC, blockingEC)
      program = (t: Transactor[Task]) => server.provideSome[Environment] { base =>
        new Clock with Console with LiveUserRepository with LiveLogger {
          val clock: Clock.Service[Any] = base.clock
          val console: Console.Service[Any] = base.console
          val config: Config = ConfigFactory.parseMap(
            Map(
              "dataSourceClassName" -> application.database.className.value,
              "dataSource.url" -> application.database.url.value,
              "dataSource.schema" -> application.database.schema.value,
              "dataSource.user" -> application.database.user.value,
              "dataSource.password" -> application.database.password.value
            ).asJava)
          override val transactor: doobie.Transactor[Task] = t
        }
      }

      run <- resXa.use(program)
    } yield run

    result
      .foldM(failure = err => putStrLn(s"Execution failed with: $err") *> ZIO.succeed(1), success = _ => ZIO.succeed(0))
  }
}
