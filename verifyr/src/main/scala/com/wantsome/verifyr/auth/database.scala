package com.wantsome

package verifyr

package auth

import com.wantsome.verifyr.auth.Repo.Env
import zio._
import doobie.implicits.javasql._
import doobie.implicits._
import doobie.refined.implicits._
import common._
import common.db._, utils._
import common.data._

object Repo {
  type Env = SettingsProvider with TransactorProvider

  trait Service {
    def saveUser(user: User): RIO[Env, Unit]

    def getCombo(c: ComboType): RIO[Env, Combo]
  }

}

trait Repo {
  def userRepo: Repo.Service
}

trait LiveRepoService extends Repo.Service {
  import Repo._

  override def saveUser(user: User): RIO[Env, Unit] =
    runDb {
      sql"""insert into users(id,email,first_name,last_name,birthday,city,phone,
      occupation,field_of_work,english_level,it_experience,experience_description,heard_from)
      values(${user.id},${user.email},${user.firstName},${user.lastName},${user.birthday},
        ${user.city},${user.phone},${user.occupation},${user.fieldOfWork},${user.englishLevel},
        ${user.itExperience},${user.experienceDescription},${user.heardFrom})""".update.run
    } >>= (res => if (res == 1) ZIO.unit else ZIO.fail(InsertFailed))

  override def getCombo(c: ComboType): RIO[Env, Combo] = runDb {
    val res = c match {
      case EnglishLevel => sql"""select id,value,label from english_level_c""".query[ComboValue].to[List]
      case Occupation => sql"""select id,value,label from occupation_c""".query[ComboValue].to[List]
      case FieldOfWork => sql"""select id,value,label from field_of_work""".query[ComboValue].to[List]
    }
    res.map(Combo)
  }
}

trait LiveRepo extends Repo {

  override def userRepo: Repo.Service = new LiveRepoService {}
}

object database {

  def saveUser[E <: Repo with Env](user: User): RIO[E, Unit] =
    ZIO.accessM[E](_.userRepo.saveUser(user))

  def getCombo[E <: Env with Repo](c: ComboType): RIO[E, Combo] =
    ZIO.accessM[E](_.userRepo.getCombo(c))
}
