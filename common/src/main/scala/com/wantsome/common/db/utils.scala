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

  // def transactor = ZIO.access[TransactorProvider](_.transactor())
  //
  // def runDb[A](trans: => ConnectionIO[A]) = transactor >>= (trans.transact(_))
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
