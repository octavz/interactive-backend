package com.wantsome

package verifyr

package repository

import java.util.UUID

import zio._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined._
import commons._
import models._
import config._
import db._
import eu.timepit.refined.types.string.{FiniteString, NonEmptyString}

object UserRepo {
  trait Env extends SettingsProvider with TransactorProvider

  trait Service {
    def saveUser(user: User): ZIO[Env, DatabaseError, Unit]
  }

}

trait UserRepo {
  def userRepo: UserRepo.Service
}

trait LiveUserRepoService extends UserRepo.Service {
  import UserRepo._
  implicit val s = implicitly[Put[FiniteString[W.`200`.T]]]

  override def saveUser(user: User): ZIO[Env, DatabaseError, Unit] =
    runDb {
      sql"""insert into users(id,email,first_name,last_name,birthday,city,phone,
      occupation,field_of_work,english_level,it_experience,experience_description,heard_from)
      values(${user.id},${user.email},${user.firstName},${user.lastName},${user.birthday},
        ${user.city},${user.phone},${user.occupation},${user.fieldOfWork},${user.englishLevel},
        ${user.itExperience},${user.experienceDescription},${user.heardFrom})""".update.run
    } >>= (res => if (res == 1) ZIO.unit else ZIO.fail(InsertFailed))

}

trait LiveUserRepo extends UserRepo {
  import UserRepo._

  override def userRepo: Service = new LiveUserRepoService {}
}
