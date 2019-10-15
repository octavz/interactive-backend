package com.wantsome

package common

package config

import doobie.util.transactor.Transactor
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import zio._
import zio.blocking.Blocking
import pureconfig._
import pureconfig.generic.auto._
import eu.timepit.refined.pureconfig._

import scala.concurrent.ExecutionContext
import common.db._

case class AppConfig(database: DatabaseConfig)

case class DatabaseConfig(
  className: Refined[String, NonEmpty],
  url: Refined[String, NonEmpty],
  schema: Refined[String, NonEmpty],
  user: Refined[String, NonEmpty],
  password: Refined[String, NonEmpty])

object SettingsProvider {

  trait Service {
    def config: Either[Throwable, AppConfig]
  }
}

trait SettingsProvider {
  val settingsProvider: SettingsProvider.Service
}

trait LiveSettingsProviderService extends SettingsProvider.Service {

  override def config: Either[Throwable, AppConfig] =
    ConfigSource.default.load[AppConfig] match {
      case Right(value) => Right(value)
      case Left(err) =>
        val errString = err.toList.map(_.description).mkString(",")
        Left(new Exception(errString))
    }

}

trait LiveSettingsProvider extends SettingsProvider {
  override val settingsProvider: SettingsProvider.Service = new LiveSettingsProviderService {}
}

object settings {

  def config[R <: SettingsProvider]: RIO[R, AppConfig] =
    ZIO.access[SettingsProvider](_.settingsProvider.config) >>= (ZIO.fromEither(_))

  def managedTransactor[R <: Blocking with SettingsProvider](
    ec: ExecutionContext): ZManaged[R, Throwable, Transactor[Task]] = {
    val io = for {
      c <- config[SettingsProvider]
      blockingEc <- ZIO.accessM[Blocking](_.blocking.blockingExecutor.map(_.asEC))
    } yield transactor.mkTransactor(c.database, ec, blockingEc)

    ZManaged.unwrap(io)
  }
}
