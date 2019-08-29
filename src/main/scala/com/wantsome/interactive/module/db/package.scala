package com.wantsome.interactive.module

import com.wantsome.interactive.model.Error
import com.wantsome.interactive.model.database.User
import zio.ZIO

package object db {

  def get(id: Long): ZIO[UserRepository, Error, User] =
    ZIO.accessM(_.repository.get(id))

  def create(user: User): ZIO[UserRepository, Error, User] =
    ZIO.accessM(_.repository.create(user))

  def delete(id: Long): ZIO[UserRepository, Error, Unit] =
    ZIO.accessM(_.repository.delete(id))
}
