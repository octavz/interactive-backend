package com.wantsome

package verifyr

package auth

import zio._
import zio.interop.catz._
import doobie._
import doobie.implicits.javasql._
import doobie.implicits._
import doobie.refined.implicits._
import common._
import common.db._, utils._
import common.data._

trait Repo {
  val userRepo: Repo.Service[Any]
}

object Repo {
  // type Env = SettingsProvider with TransactorProvider

  trait Service[R] {
    def saveUser(user: User): RIO[R, Unit]

    def getCombo(c: ComboType): RIO[R, Combo]
  }

  object > extends Service[Repo] {

    def saveUser(user: User) = ZIO.accessM(_.userRepo.saveUser(user))

    def getCombo(c: ComboType) = ZIO.accessM(_.userRepo.getCombo(c))
  }
}

trait LiveRepo extends Repo {

  val transactorProvider: TransactorProvider.Service

  def runDb[A](trans: => ConnectionIO[A]) = trans.transact(transactorProvider.transactor)

  override val userRepo = new Repo.Service[Any] {
    override def saveUser(user: User) =
      runDb {
        sql"""insert into users(id,email,first_name,last_name,birthday,city,phone,
      occupation,field_of_work,english_level,it_experience,experience_description,heard_from)
      values(${user.id},${user.email},${user.firstName},${user.lastName},${user.birthday},
        ${user.city},${user.phone},${user.occupation},${user.fieldOfWork},${user.englishLevel},
        ${user.itExperience},${user.experienceDescription},${user.heardFrom})""".update.run
      } >>= (res => if (res == 1) ZIO.unit else ZIO.fail(InsertFailed))

    override def getCombo(c: ComboType) = runDb {
      val res = c match {
        case EnglishLevel =>
          sql"""select id,value,label from english_level_c""".query[ComboValue].to[List]
        case Occupation =>
          sql"""select id,value,label from occupation_c""".query[ComboValue].to[List]
        case FieldOfWork =>
          sql"""select id,value,label from field_of_work_c""".query[ComboValue].to[List]
      }
      res.map(Combo)
    }
  }
}
