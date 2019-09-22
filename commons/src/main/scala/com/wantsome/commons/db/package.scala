package com.wantsome.commons

import java.util.UUID

import doobie._
import doobie.implicits._
import zio._
import zio.interop.catz._
import eu.timepit.refined.types.string._
import io.circe.generic.JsonCodec

package object db {
  sealed trait DatabaseError
  final case class DatabaseThrowable(t: Throwable) extends DatabaseError
  case object InsertFailed extends DatabaseError

  type DbString = NonEmptyString //And FiniteString[W.`200`.T]

  type Id = UUID
  type ComboId = Short

  sealed trait Combos
  case object Occupation
  case object FieldOfWork
  case object EnglishLevel
  case object HeardFrom

  case class Combo(id: ComboId, value: DbString, label: DbString)

  def transactor[R <: TransactorProvider] =
    ZIO.accessM[R](_.transactor())

  def runDb[R <: TransactorProvider, A](trans: => ConnectionIO[A]): ZIO[R, DatabaseError, A] =
    transactor[R] >>= (trans.transact(_).mapError(DatabaseThrowable))
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
