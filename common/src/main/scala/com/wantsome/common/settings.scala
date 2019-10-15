package com.wantsome

package common

import zio._
import zio.blocking.Blocking
import doobie.util.transactor.Transactor
import scala.concurrent.ExecutionContext

import com.wantsome.common.db.transactor

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
