package com.wantsome

package verifyr

package auth

import zio._
import common.data._

trait Auth {
  val auth: Auth.Service
}

object Auth {
  type Env = Repo.Env with Repo

  trait Service {
    def registerUser(user: User): RIO[Env, User]
    def combos: RIO[Env, Map[ComboType, Combo]]
  }
}

trait AuthLiveService extends Auth.Service {
  import database._

  override def registerUser(user: User): RIO[Auth.Env, User] =
    registerUser(user)

  override def combos: RIO[Auth.Env, Map[ComboType, Combo]] = {
    for {
      e <- getCombo(EnglishLevel)
      o <- getCombo(Occupation)
      f <- getCombo(FieldOfWork)
    } yield Map(EnglishLevel -> e, Occupation -> o, FieldOfWork -> f)

  }
}

trait LiveAuth extends Auth {

  override val auth: Auth.Service = new AuthLiveService {}
}

object service {

  def combos[R <: Auth.Env with Auth]: RIO[R, Map[ComboType, Combo]] =
    ZIO.accessM[R](_.auth.combos)

}
