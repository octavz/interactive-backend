package com.wantsome

package verifyr

package repository

import com.wantsome.commons.models.User
import config.ConfigProvider
import zio._

object UserRepo {
  trait Env extends ConfigProvider

  trait Service {
    def saveUser(): RIO[Env, User]
  }

}
