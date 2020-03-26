package com.wantsome.interactive

import zio.{test => _}
import zio.interop.catz._
import doobie._
import doobie.implicits._

import com.wantsome.common.db.TransactorProvider

object util {
  implicit class DoobieHelper[A](q: ConnectionIO[A]) {
    def zio =
      TransactorProvider.transactor >>= { xa =>
        (q.transact(xa))
      }
  }

}
