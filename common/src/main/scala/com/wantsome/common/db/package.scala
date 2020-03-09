package com.wantsome.common

import zio._
import zio.interop.catz._
import zio.blocking.Blocking
import doobie._
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import eu.timepit.refined.auto._
import scala.concurrent.ExecutionContext

import com.wantsome.common.config._
import data._

package object db {
  type TransactorProvider = Has[TransactorProvider.Service]

  object TransactorProvider {

    trait Service {
      val transactor: Transactor[Task]
    }

    def transactor =
      ZIO.access[TransactorProvider](_.get.transactor)

    def live(t: Transactor[Task]): ZLayer.NoDeps[Nothing, TransactorProvider] =
      ZLayer.succeed(new Service {
        override val transactor: Transactor[Task] = t
      })

  }

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
