package com.wantsome

package verifyr

package auth

import zio._
import common.data._
import common._
import zio.macros.annotation.accessible

@accessible
trait AuthProvider {
  val authProvider: AuthProvider.Service[Any]
}

object AuthProvider {

  trait Service[R] {
    def registerUser(user: User): RIO[R, User]
    def combos: RIO[R, Map[ComboType, Combo]]
  }
}

package object auth extends AuthProvider.Accessors

trait LiveAuthProvider extends AuthProvider {
  val dates: Dates.Service[Any]
  val repo: Repo.Service[Any]
  val settingsProvider: SettingsProvider

  val authProvider = new AuthProvider.Service[Any] {
    override def registerUser(user: User) =
      for {
        id            <- Id.gen
        localDT       <- dates.nowUTC
        nowTs         <- dates.toTimestamp(localDT)
        invitationExp <- settingsProvider.zioConfig.map(_.invitationExpirationSeconds)
        invitation = Invitation(id, user.id, nowTs)
        res <- repo.insertUser(user, List("student")).as(user)
      } yield res

    override def combos = {
      for {
        e <- repo.getCombo(EnglishLevel)
        o <- repo.getCombo(Occupation)
        f <- repo.getCombo(FieldOfWork)
      } yield Map(EnglishLevel -> e, Occupation -> o, FieldOfWork -> f)
    }
  }
}
