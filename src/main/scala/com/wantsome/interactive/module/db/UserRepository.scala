package com.wantsome.interactive.module.db

import com.wantsome.interactive.model.Error
import com.wantsome.interactive.model.database.User
import zio.ZIO

trait UserRepository {

  val repository: UserRepository.Service
}

object UserRepository {

  trait Service {

    def get(id: Long): ZIO[Any, Error, User]

    def create(user: User): ZIO[Any, Error, User]

    def delete(id: Long): ZIO[Any, Error, Unit]
  }
}
