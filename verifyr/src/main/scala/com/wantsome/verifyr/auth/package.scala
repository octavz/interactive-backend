package com.wantsome.verifyr

import com.wantsome.common.config.SettingsProvider
import zio._
import com.wantsome.common.data._
import com.wantsome.common.dates._
import com.wantsome.verifyr.auth.store._

package object auth {
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

    val live: ZLayer[Repo with SettingsProvider with Dates, Nothing, AuthProvider] =
      ZLayer.fromFunction((e: Repo with SettingsProvider with Dates) =>
        new LiveAuthProvider {
          override val dates = e.get[Dates.Service]
          override val repo = e.get[Repo.Service]
          override val settingsProvider = e.get[SettingsProvider.Service]
        })
  }
}
