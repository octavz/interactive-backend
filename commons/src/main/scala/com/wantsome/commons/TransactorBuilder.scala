package com.wantsome.commons

import doobie.Transactor
import doobie.hikari.HikariTransactor
import zio._
import zio.interop.catz._
import models._
import eu.timepit.refined.auto._
import scala.concurrent.ExecutionContext

object TransactorBuilder {

  def mkTransactor(
    config: Database,
    connectEC: ExecutionContext,
    transactEC: ExecutionContext): Managed[Throwable, Transactor[Task]] = {
    val xa = HikariTransactor
      .newHikariTransactor[Task](
        config.className,
        config.url,
        config.user,
        config.password,
        connectEC,
        cats.effect.Blocker.liftExecutionContext(transactEC))
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
}
