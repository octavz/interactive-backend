package com.wantsome

package common

package db

import doobie.Transactor
import zio._

object TransactorProvider {

  trait Service {
    def apply(): Transactor[Task]
  }
}

trait TransactorProvider {
  val transactor: TransactorProvider.Service
}
