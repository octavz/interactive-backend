package com.wantsome.commons

package db

import doobie._
import zio._

object TransactorProvider {

  trait Service {
    def apply(): IO[DatabaseError, Transactor[Task]]
  }

}

trait TransactorProvider {
  val transactor: TransactorProvider.Service
}
