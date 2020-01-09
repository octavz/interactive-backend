package com.wantsome

package verifyr

package auth

import zio._
import zio.interop.catz._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.refined.implicits._
import cats.implicits._
import common._
import common.data._

trait Repo {
  val userRepo: Repo.Service[Any]
}

object Repo {

  trait Service[R] {
    def insertUser(user: User, groups: List[Id]): RIO[R, Unit]
    def getCombo(c: ComboType): RIO[R, Combo]
    def groups: RIO[R, List[Group]]
  }

  object > extends Service[Repo] {
    def insertUser(user: User, groups: List[Id]) = ZIO.accessM(_.userRepo.insertUser(user, groups))
    def getCombo(c: ComboType) = ZIO.accessM(_.userRepo.getCombo(c))
    def groups = ZIO.accessM(_.userRepo.groups)
  }
}

sealed trait DatabaseError extends Throwable
case object InsertFailed extends DatabaseError

trait LiveRepo extends Repo {

  val transactorProvider: TransactorProvider

  def runDb[A](trans: => ConnectionIO[A]) = trans.transact(transactorProvider.transactor)

  override val userRepo = new Repo.Service[Any] {
    override def insertUser(user: User, groups: List[Id]) =
      runDb {
        val userGroups = groups.map(g => UserGroup(userId = user.id, groupId = g))
        for {
          u <- sql"""insert into users(id,email,first_name,last_name,birthday,city,phone, 
            occupation,field_of_work,english_level,it_experience,experience_description,heard_from)
            values(${user.id},${user.email},${user.firstName},${user.lastName},${user.birthday},
            ${user.city},${user.phone},${user.occupation},${user.fieldOfWork},${user.englishLevel},
            ${user.itExperience},${user.experienceDescription},${user.heardFrom})""".update.run
          g <- Update[UserGroup]("insert into groups_users(user_id, group_id) values(?,?)")
                .updateMany(userGroups)
        } yield (u, g)
      } >>= (res => if (res == (1, groups.size)) ZIO.unit else ZIO.fail(InsertFailed))

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

    override def groups = runDb {
      sql"select id, description from groups".query[Group].to[List]
    }

  }
}
