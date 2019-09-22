package com.wantsome.commons

import java.util.UUID

import doobie._
import doobie.implicits._
import zio._
import zio.interop.catz._
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean._
import eu.timepit.refined.types.string._

package object db {
  sealed trait DatabaseError
  final case class DatabaseThrowable(t: Throwable) extends DatabaseError
  case object InsertFailed extends DatabaseError

  type DbString = NonEmptyString //And FiniteString[W.`200`.T]

  case class Id(value: UUID) extends AnyVal
  case class ComboId(value: Short) extends AnyVal

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
