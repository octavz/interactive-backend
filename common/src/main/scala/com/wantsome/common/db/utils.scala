package com.wantsome

package common

package db

import doobie._
import doobie.implicits._
import zio._
import zio.interop.catz._

object utils {

  sealed trait DatabaseError extends Throwable
  case object InsertFailed extends DatabaseError

  def transactor[R <: TransactorProvider]: ZIO[R, Nothing, Transactor[Task]] =
    ZIO.access[R](_.transactor())

  def runDb[R <: TransactorProvider, A](trans: => ConnectionIO[A]): RIO[R, A] =
    transactor[R] >>= (trans.transact(_))
  /*
  def appConfig[R <: SettingsProvider]: RIO[R, AppConfig] =
    ZIO.accessM[R](_.settings())
  def runDbWithConfig[R <: TransactorProvider with SettingsProvider, A](
    trans: AppConfig => ConnectionIO[A]): RIO[R, A] = {
    appConfig[R] >>= { conf =>
      transactor[R] >>= (trans(conf).transact(_))
    }
  }
 */
}
