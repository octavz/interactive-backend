package com.wantsome.commons.db

import com.wantsome.commons.config.DatabaseConfig
import doobie.Transactor
import doobie.hikari.HikariTransactor
import eu.timepit.refined.auto._
import zio._
import zio.interop.catz._

import scala.concurrent.ExecutionContext

object TransactorBuilder {

  def mkTransactor(
    config: DatabaseConfig,
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
