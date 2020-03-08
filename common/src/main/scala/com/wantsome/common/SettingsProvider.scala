package com.wantsome

package common

import zio.{IO, ZIO}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import pureconfig._
import pureconfig.generic.auto._
import eu.timepit.refined.pureconfig._

case class AppConfig(invitationExpirationSeconds: Long, database: DatabaseConfig)

case class DatabaseConfig(
  className: Refined[String, NonEmpty],
  url: Refined[String, NonEmpty],
  schema: Refined[String, NonEmpty],
  user: Refined[String, NonEmpty],
  password: Refined[String, NonEmpty])

trait SettingsProvider {
  val config: Either[Throwable, AppConfig]

  val zioConfig: IO[Throwable, AppConfig] = ZIO.fromEither(config)
}

trait LiveSettingsProvider extends SettingsProvider {

  override val config: Either[Throwable, AppConfig] =
    ConfigSource.default.load[AppConfig] match {
      case Right(value) => Right(value)
      case Left(err) =>
        val errString = err.toList.map(_.description).mkString(",")
        Left(new Exception(errString))
    }
}

object LiveSettingsProvider extends LiveSettingsProvider
