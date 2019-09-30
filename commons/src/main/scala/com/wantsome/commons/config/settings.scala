package com.wantsome

package commons

package config

import com.wantsome.commons.db.TransactorBuilder
import doobie.util.transactor.Transactor
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import zio._
import zio.blocking.Blocking
import pureconfig._
import pureconfig.generic.auto._
import eu.timepit.refined.pureconfig._

import scala.concurrent.ExecutionContext

case class AppConfig(database: DatabaseConfig)

case class DatabaseConfig(
  className: Refined[String, NonEmpty],
  url: Refined[String, NonEmpty],
  schema: Refined[String, NonEmpty],
  user: Refined[String, NonEmpty],
  password: Refined[String, NonEmpty])

object SettingsProvider {

  trait Service {
    def config: Task[AppConfig]
    def transactor(ec: ExecutionContext): ZManaged[Blocking, Throwable, Transactor[Task]]
  }
}

trait SettingsProvider {
  val settingsProvider: SettingsProvider.Service
}

trait LiveSettingsProviderService extends SettingsProvider.Service {

  override def config: Task[AppConfig] =
    ZIO.effect(ConfigSource.default.load[AppConfig]).flatMap {
      case Right(value) =>
        Task.succeed(value)
      case Left(err) =>
        val errString = err.toList.map(_.description).mkString(",")
        Task.fail(new Exception(errString))
    }

  override def transactor(ec: ExecutionContext): ZManaged[Blocking, Throwable, Transactor[Task]] = {
    val io = for {
      c <- config
      blockingEc <- ZIO.accessM[Blocking](_.blocking.blockingExecutor.map(_.asEC))
    } yield TransactorBuilder.mkTransactor(c.database, ec, blockingEc)
    ZManaged.unwrap(io)
  }

}

trait LiveSettingsProvider extends SettingsProvider {
  override val settingsProvider: SettingsProvider.Service = new LiveSettingsProviderService {}
}

object settings {

  def config[R <: SettingsProvider]: RIO[R, AppConfig] =
    ZIO.accessM[SettingsProvider](_.settingsProvider.config)

  def transactor[R <: Blocking with SettingsProvider](ec: ExecutionContext): ZManaged[R, Throwable, Transactor[Task]] =
    ZManaged.unwrap(ZIO.access[R](_.settingsProvider.transactor(ec)))

}
