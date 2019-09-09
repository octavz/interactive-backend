package com.wantsome

package verifyr

import com.wantsome.commons.models.User
import zio.RIO

object Registrator {
  trait Env

  trait Service {
    def registerUser(): RIO[Env, User]
  }
}
