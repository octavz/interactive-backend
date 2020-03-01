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
import common.{data, _}
import common.data._
import zio.macros.annotation.accessible

trait StoreBackend {
  def sqlInsertInvitation(invitation: Invitation): doobie.ConnectionIO[Int]
  def sqlInsertUser(user: User): doobie.ConnectionIO[Int]
  def sqlInsertUserGroups(userGroups: List[UserGroup]): doobie.ConnectionIO[Int]
  def sqlGetEnglishCombo: doobie.ConnectionIO[List[data.ComboValue]]
  def sqlGetOccupationCombo: doobie.ConnectionIO[List[data.ComboValue]]
  def sqlGetFieldOfWorkCombo: doobie.ConnectionIO[List[data.ComboValue]]
  def sqlGetAllGroups: doobie.ConnectionIO[List[data.Group]]
}

trait SqlBackend extends StoreBackend {

  override def sqlInsertUser(user: User): doobie.ConnectionIO[Int] =
    sql"""insert into users(id,email,first_name,last_name,birthday,city,phone,
            occupation,field_of_work,english_level,it_experience,experience_description,heard_from)
            values(${user.id},${user.email},${user.firstName},${user.lastName},${user.birthday},
            ${user.city},${user.phone},${user.occupation},${user.fieldOfWork},${user.englishLevel},
            ${user.itExperience},${user.experienceDescription},${user.heardFrom})""".update.run

  override def sqlInsertUserGroups(userGroups: List[UserGroup]): doobie.ConnectionIO[Int] =
    Update[UserGroup]("insert into groups_users(user_id, group_id) values(?,?)").updateMany(userGroups)

  override def sqlInsertInvitation(invitation: Invitation): doobie.ConnectionIO[Int] =
    sql"insert into invitations(id,user_id,expires_at) values(${invitation.id}, ${invitation.userId}, ${invitation.expiresAt})".update.run

  override def sqlGetEnglishCombo: doobie.ConnectionIO[List[data.ComboValue]] =
    sql"""select id,value,label from english_level_c""".query[ComboValue].to[List]
  override def sqlGetOccupationCombo: doobie.ConnectionIO[List[data.ComboValue]] =
    sql"""select id,value,label from occupation_c""".query[ComboValue].to[List]
  override def sqlGetFieldOfWorkCombo: doobie.ConnectionIO[List[data.ComboValue]] =
    sql"""select id,value,label from field_of_work_c""".query[ComboValue].to[List]

  override def sqlGetAllGroups: doobie.ConnectionIO[List[data.Group]] =
    sql"select id, description from groups".query[Group].to[List]

}

@accessible(">")
trait Repo {
  val repo: Repo.Service[Any]
}

object Repo {

  trait Service[R] {
    def insertUser(user: User, groups: List[Id]): RIO[R, Unit]
    def insertUserWithInvitation(user: User, groups: List[Id], invitation: Invitation): RIO[R, Unit]
    def insertInvitation(invitation: Invitation): RIO[R, Unit]
    def getCombo(c: ComboType): RIO[R, Combo]
    def groups: RIO[R, List[Group]]
  }

}

sealed trait DatabaseError extends Throwable
case class InsertFailed(message: String) extends DatabaseError

trait LiveRepo extends Repo {

  val transactorProvider: TransactorProvider
  val backend: StoreBackend

  def runDb[A](trans: => ConnectionIO[A]) = trans.transact(transactorProvider.transactor)

  override val repo = new Repo.Service[Any] {

    override def insertUser(user: User, groups: List[Id]) =
      runDb {
        val userGroups = groups.map(g => UserGroup(userId = user.id, groupId = g))
        for {
          u <- backend.sqlInsertUser(user)
          g <- backend.sqlInsertUserGroups(userGroups)
        } yield (u, g)
      } >>= (res =>
        if (res == (1, groups.size)) ZIO.unit else ZIO.fail(InsertFailed(s"User insert failed for $user with $groups")))

    override def insertUserWithInvitation(user: User, groups: List[Id], invitation: Invitation) = {
      val db = runDb {
        val userGroups = groups.map(g => UserGroup(userId = user.id, groupId = g))
        for {
          u <- backend.sqlInsertUser(user)
          g <- backend.sqlInsertUserGroups(userGroups)
          i <- backend.sqlInsertInvitation(invitation)
        } yield (u, g, i)
      } >>= (res =>
        if (res == (1, groups.size, 1)) ZIO.unit
        else ZIO.fail(InsertFailed(s"User insert failed for $user with $groups with $invitation")))

      // for {
      //  _ <- db
      // _ <- emailBackend.sendInvitationEmail()
      // }
      db
    }

    override def insertInvitation(invitation: Invitation) =
      runDb {
        backend.sqlInsertInvitation(invitation)
      } >>= (res => if (res == 1) ZIO.unit else ZIO.fail(InsertFailed(s"Invitation insert failed for: $invitation")))

    override def getCombo(c: ComboType) = runDb {
      val res = c match {
        case EnglishLevel => backend.sqlGetEnglishCombo
        case Occupation => backend.sqlGetOccupationCombo
        case FieldOfWork => backend.sqlGetFieldOfWorkCombo
      }
      res.map(Combo)
    }

    override def groups = runDb {
      backend.sqlGetAllGroups
    }

  }
}
