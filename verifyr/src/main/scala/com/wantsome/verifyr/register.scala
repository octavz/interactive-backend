package com.wantsome.verifyr

import com.wantsome.common.config.SettingsProvider
import zio._
import com.wantsome.common.data._
import com.wantsome.common.dates._
import store._

object register {
  type AuthProvider = Has[AuthProvider.Service]

  object AuthProvider {

    trait Service {
      def registerUser(user: User): Task[User]

      def combos: Task[Map[ComboType, Combo]]
    }

    def registerUser(user: User) =
      ZIO.accessM[AuthProvider](_.get.registerUser(user))

    def combos =
      ZIO.accessM[AuthProvider](_.get.combos)

    val live
        : ZLayer[Repo with SettingsProvider with Dates, Nothing, AuthProvider] =
      ZLayer.fromFunction((e: Repo with SettingsProvider with Dates) =>
        new LiveAuthProvider {
          override val dates = e.get[Dates.Service]
          override val repo = e.get[Repo.Service]
          override val settingsProvider = e.get[SettingsProvider.Service]
        }
      )
  }

  trait LiveAuthProvider extends AuthProvider.Service {
    val dates: Dates.Service
    val repo: Repo.Service
    val settingsProvider: SettingsProvider.Service

    override def registerUser(user: User) =
      for {
        id <- Id.gen
        localDT <- dates.nowUTC
        nowTs <- dates.toTimestamp(localDT)
        invitationExp <- settingsProvider.config.map(
          _.invitationExpirationSeconds
        )
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
