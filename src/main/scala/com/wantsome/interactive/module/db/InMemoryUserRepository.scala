package com.wantsome.interactive.module.db

import com.wantsome.interactive.model.{Error, NotFoundError}
import com.wantsome.interactive.model.database.User
import zio.{Ref, ZIO}

final case class InMemoryUserRepository(ref: Ref[Map[Long, User]]) extends UserRepository.Service {

  def get(id: Long): ZIO[Any, Error, User] =
    for {
      user <- ref.get.map(_.get(id))
      u <- user match {
        case Some(s) => ZIO.succeed(s)
        case None => ZIO.fail(NotFoundError(s"Not found a user by id = $id"))
      }
    } yield {
      u
    }

  def create(user: User): ZIO[Any, Error, User] = ref.update(map => map.+(user.id -> user)).map(_ => user)

  def delete(id: Long): ZIO[Any, Error, Unit] = ref.update(map => map.-(id)).unit
}
