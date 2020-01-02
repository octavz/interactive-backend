package com.wantsome

package common

import zio._
import zio.blocking.Blocking
import doobie.util.transactor.Transactor
import scala.concurrent.ExecutionContext

import com.wantsome.common.db.transactor

object settings {

  def managedTransactor(
    ec: ExecutionContext): ZManaged[SettingsProvider with Blocking, Throwable, Transactor[Task]] = {
    val io = for {
      c <- SettingsProvider.>.config
      blockingEc <- ZIO.accessM[Blocking](_.blocking.blockingExecutor.map(_.asEC))
    } yield transactor.mkTransactor(c.database, ec, blockingEc)

    ZManaged.unwrap(io)
  }
}
