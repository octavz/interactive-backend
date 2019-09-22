package com.wantsome

package commons

package config

import doobie.util.transactor.Transactor
import zio._
import db._

object SettingsProvider {

  trait Service {
    def config: UIO[AppConfig]
    def combos: UIO[Map[Combos, Combo]]
    def transactor: Transactor[Task]
  }
}

trait SettingsProvider {
  val settingsProvider: SettingsProvider.Service
}
