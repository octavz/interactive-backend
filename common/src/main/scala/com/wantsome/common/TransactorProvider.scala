package com.wantsome

package common

import doobie.Transactor
import zio._

object TransactorProvider {

  trait Service {
    val transactor: Transactor[Task]
  }

  object > {
    val transactor = ZIO.access[TransactorProvider](_.transactor)
  }
}

trait TransactorProvider {
  val transactor: TransactorProvider.Service
}
