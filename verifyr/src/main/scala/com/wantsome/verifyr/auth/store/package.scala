package com.wantsome.verifyr.auth

import zio._
import com.wantsome.common.data._
import com.wantsome.common.db.TransactorProvider

package object store {
  type Repo = Has[Repo.Service]

  sealed trait DatabaseError extends Throwable

  case class InsertFailed(message: String) extends DatabaseError

  object Repo {

    trait Service {
      def insertUser(user: User, groups: List[Id]): Task[Unit]

      def insertUserWithInvitation(user: User, groups: List[Id], invitation: Invitation): Task[Unit]

      def insertInvitation(invitation: Invitation): Task[Unit]

      def getCombo(c: ComboType): Task[Combo]

      def groups: Task[List[Group]]
    }

    def insertUser(user: User, groups: List[Id]) =
      ZIO.accessM[Repo](_.get.insertUser(user, groups))

    def insertUserWithInvitation(user: User, groups: List[Id], invitation: Invitation) =
      ZIO.accessM[Repo](_.get.insertUserWithInvitation(user, groups, invitation))

    def insertInvitation(invitation: Invitation) =
      ZIO.accessM[Repo](_.get.insertInvitation(invitation))

    def getCombo(c: ComboType) =
      ZIO.accessM[Repo](_.get.getCombo(c))

    def groups =
      ZIO.accessM[Repo](_.get.groups)

    def live(b: StoreBackend): ZLayer[TransactorProvider, Nothing, Repo] =
      ZLayer.fromFunction((s: TransactorProvider) =>
        new LiveRepo {
          override val transactorProvider: TransactorProvider.Service = s.get[TransactorProvider.Service]
          override val backend: StoreBackend = b
        })
  }
}
