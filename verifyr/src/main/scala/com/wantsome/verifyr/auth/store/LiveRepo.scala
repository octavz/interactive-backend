package com.wantsome.verifyr.auth

package store

import doobie._
import doobie.implicits._
import zio._
import zio.interop.catz._
import com.wantsome.common.data._
import com.wantsome.common.db.TransactorProvider

trait LiveRepo extends Repo.Service {

  val transactorProvider: TransactorProvider.Service
  val backend: StoreBackend

  def runDb[A](trans: => ConnectionIO[A]) = trans.transact(transactorProvider.transactor)

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
