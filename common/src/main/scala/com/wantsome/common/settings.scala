package com.wantsome

package common

import zio._
import zio.blocking.Blocking
import doobie.util.transactor.Transactor
import scala.concurrent.ExecutionContext

import com.wantsome.common.db.transactor

object settings {

  def managedTransactor(
    settingsProvider: SettingsProvider,
    ec: ExecutionContext): ZManaged[Blocking, Throwable, Transactor[Task]] = {
    val io = for {
      c          <- settingsProvider.zioConfig
      blockingEc <- ZIO.accessM[Blocking](_.blocking.blockingExecutor.map(_.asEC))
    } yield transactor.mkTransactor(c.database, ec, blockingEc)

    ZManaged.unwrap(io)
  }
}
