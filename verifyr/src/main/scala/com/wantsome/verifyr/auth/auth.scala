package com.wantsome

package verifyr

package auth

import zio._
import common.data._

trait AuthProvider {
  val auth: AuthProvider.Service[Any]
}

object AuthProvider {

  trait Service[R] {
    def registerUser(user: User): RIO[R, User]
    def combos: RIO[R, Map[ComboType, Combo]]
  }

  object > extends Service[AuthProvider] {
    def registerUser(user: User) = ZIO.accessM[AuthProvider](_.auth.registerUser(user))
    def combos = ZIO.accessM[AuthProvider](_.auth.combos)
  }
}

trait LiveAuthProvider extends AuthProvider {
  val repo: Repo.Service[Any]

  val auth = new AuthProvider.Service[Any] {
    override def registerUser(user: User) =
      repo.insertUser(user, List("student")).as(user)

    override def combos = {
      for {
        e <- repo.getCombo(EnglishLevel)
        o <- repo.getCombo(Occupation)
        f <- repo.getCombo(FieldOfWork)
      } yield Map(EnglishLevel -> e, Occupation -> o, FieldOfWork -> f)
    }
  }
}
