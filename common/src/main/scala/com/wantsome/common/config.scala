package com.wantsome.common

import data._

import pureconfig.error._
import pureconfig.generic.auto._
import eu.timepit.refined.pureconfig._
import pureconfig.ConfigSource
import zio.ZLayer.NoDeps
import zio._

object config {

  type SettingsProvider = Has[SettingsProvider.Service]

  object SettingsProvider {

    trait Service {
      val config: Task[AppConfig]
    }

    def live: NoDeps[Nothing, SettingsProvider] = ZLayer.succeed(
      new Service {
        override val config =
          ConfigSource.default.load[AppConfig] match {
            case Right(value) => Task.succeed(value)
            case Left(err) =>
              val errString = err.toList.map(_.description).mkString(",")
              Task.fail(new Exception(errString))
          }
      }
    )

    def config =
      ZIO.accessM[SettingsProvider](_.get.config)
  }

}
