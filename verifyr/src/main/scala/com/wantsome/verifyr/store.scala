package com.wantsome.verifyr

import zio._
import zio.interop.catz._
import doobie._
import doobie.implicits._
import com.wantsome.common.data._
import com.wantsome.common.db.TransactorProvider
import sql._

object store {

  type Repo = Has[Repo.Service]

  sealed trait DatabaseError extends Throwable

  case class InsertFailed(message: String) extends DatabaseError

  object Repo {

    trait Service {
      def insertUser(user: User, groups: List[Id]): Task[Unit]

      def insertUserWithInvitation(
          user: User,
          groups: List[Id],
          invitation: Invitation
      ): Task[Unit]

      def insertInvitation(invitation: Invitation): Task[Unit]

      def getCombo(c: ComboType): Task[Combo]

      def groups: Task[List[Group]]
    }

    def insertUser(user: User, groups: List[Id]) =
      ZIO.accessM[Repo](_.get.insertUser(user, groups))

    def insertUserWithInvitation(
        user: User,
        groups: List[Id],
        invitation: Invitation
    ) =
      ZIO.accessM[Repo](
        _.get.insertUserWithInvitation(user, groups, invitation)
      )

    def insertInvitation(invitation: Invitation) =
      ZIO.accessM[Repo](_.get.insertInvitation(invitation))

    def getCombo(c: ComboType) =
      ZIO.accessM[Repo](_.get.getCombo(c))

    def groups =
      ZIO.accessM[Repo](_.get.groups)

    val live: ZLayer[TransactorProvider with StoreBackend, Nothing, Repo] =
      ZLayer.fromFunction((env: TransactorProvider with StoreBackend) =>
        new LiveRepo {
          override val transactorProvider: TransactorProvider.Service =
            env.get[TransactorProvider.Service]
          override val backend = env.get[StoreBackend.Service]
        }
      )
  }

  trait LiveRepo extends Repo.Service {

    val transactorProvider: TransactorProvider.Service
    val backend: StoreBackend.Service

    def runDb[A](trans: => ConnectionIO[A]) =
      trans.transact(transactorProvider.transactor)

    override def insertUser(user: User, groups: List[Id]) =
      runDb {
        val userGroups =
          groups.map(g => UserGroup(userId = user.id, groupId = g))
        for {
          u <- backend.sqlInsertUser(user)
          g <- backend.sqlInsertUserGroups(userGroups)
        } yield (u, g)
      } >>= (res =>
        if (res == (1, groups.size)) ZIO.unit
        else
          ZIO.fail(InsertFailed(s"User insert failed for $user with $groups"))
      )

    override def insertUserWithInvitation(
        user: User,
        groups: List[Id],
        invitation: Invitation
    ) = {
      val db = runDb {
        val userGroups =
          groups.map(g => UserGroup(userId = user.id, groupId = g))
        for {
          u <- backend.sqlInsertUser(user)
          g <- backend.sqlInsertUserGroups(userGroups)
          i <- backend.sqlInsertInvitation(invitation)
        } yield (u, g, i)
      } >>= (res =>
        if (res == (1, groups.size, 1)) ZIO.unit
        else
          ZIO.fail(
            InsertFailed(
              s"User insert failed for $user with $groups with $invitation"
            )
          )
      )

      // for {
      //  _ <- db
      // _ <- emailBackend.sendInvitationEmail()
      // }
      db
    }

    override def insertInvitation(invitation: Invitation) =
      runDb {
        backend.sqlInsertInvitation(invitation)
      } >>= (res =>
        if (res == 1) ZIO.unit
        else
          ZIO.fail(InsertFailed(s"Invitation insert failed for: $invitation"))
      )

    override def getCombo(c: ComboType) = runDb {
      val res = c match {
        case EnglishLevel => backend.sqlGetEnglishCombo
        case Occupation   => backend.sqlGetOccupationCombo
        case FieldOfWork  => backend.sqlGetFieldOfWorkCombo
      }
      res.map(Combo)
    }

    override def groups = runDb {
      backend.sqlGetAllGroups
    }
  }
}
