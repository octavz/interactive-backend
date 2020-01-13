package com.wantsome

package verifyr

package auth

import zio._
import zio.clock.Clock
import zio.interop.catz._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.refined.implicits._
import cats.implicits._
import common._
import common.data._
import zio.macros.annotation.accessible

@accessible(">")
trait Repo {
  val repo: Repo.Service[Any]
}

object Repo {

  trait Service[R] {
    def insertUser(user: User, groups: List[Id]): RIO[R, Unit]
    def insertInvitation(invitation: Invitation): RIO[R, Unit]
    def getCombo(c: ComboType): RIO[R, Combo]
    def groups: RIO[R, List[Group]]
  }

}

sealed trait DatabaseError extends Throwable
case class InsertFailed(message: String) extends DatabaseError

trait LiveRepo extends Repo {

  val transactorProvider: TransactorProvider

  def runDb[A](trans: => ConnectionIO[A]) = trans.transact(transactorProvider.transactor)

  override val repo = new Repo.Service[Any] {
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
      } >>= (res =>
        if (res == (1, groups.size)) ZIO.unit else ZIO.fail(InsertFailed(s"User insert failed for $user with $groups")))

    override def insertInvitation(invitation: Invitation) =
      runDb {
        sql"""insert into invitations(id,user_id,expires_at)
            values(${invitation.id},${invitation.userId},${invitation.expiresAt})""".update.run
      } >>= (res => if (res == 1) ZIO.unit else ZIO.fail(InsertFailed(s"Invitation insert failed for: $invitation")))

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
