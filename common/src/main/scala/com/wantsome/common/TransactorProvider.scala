package com.wantsome

package common

import doobie.Transactor
import zio._

trait TransactorProvider {
  val transactor: Transactor[Task]
}
