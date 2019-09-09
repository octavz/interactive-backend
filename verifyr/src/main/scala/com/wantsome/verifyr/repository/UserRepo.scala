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

trait UserRepo {
  def userRepo: UserRepo.Service
}

trait LiveUserRepoService extends UserRepo.Service {
  import UserRepo._

  override def saveUser(): RIO[Env, User] = ???
}

trait LiveUserRepo extends UserRepo {
  import UserRepo._

  override def userRepo: Service = new LiveUserRepoService {}
}
