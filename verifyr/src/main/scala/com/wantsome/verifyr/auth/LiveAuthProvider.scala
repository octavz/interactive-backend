package com.wantsome.verifyr.auth

import com.wantsome.common.config.SettingsProvider
import com.wantsome.common.data._
import com.wantsome.common.dates._
import com.wantsome.verifyr.auth.store._

trait LiveAuthProvider extends AuthProvider.Service {
  val dates: Dates.Service
  val repo: Repo.Service
  val settingsProvider: SettingsProvider.Service

  override def registerUser(user: User) =
    for {
      id            <- Id.gen
      localDT       <- dates.nowUTC
      nowTs         <- dates.toTimestamp(localDT)
      invitationExp <- settingsProvider.config.map(_.invitationExpirationSeconds)
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
