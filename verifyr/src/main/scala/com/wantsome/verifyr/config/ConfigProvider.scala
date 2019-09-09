package com.wantsome

package verifyr

package config

import zio._

object ConfigProvider {

  trait Service {
    def apply(): Task[AppConfig]
  }

}

trait ConfigProvider {
  val config: ConfigProvider.Service
}
