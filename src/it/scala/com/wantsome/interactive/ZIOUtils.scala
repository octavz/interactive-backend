package com.wantsome.interactive

import zio.{test => _, _}
import zio.interop.catz._

import doobie._
import doobie.implicits._

import eu.timepit.refined.auto._
import com.github.mlangc.slf4zio.api._

object ZIOUtils extends LoggingSupport {
  implicit class DoobieHelper[A](q: ConnectionIO[A]) {

    def zio[A] =
      for {
        xa  <- ZIO.access[TestContext](_.transactor)
        _   <- logger.infoIO(s"Running query with config: ${xa.kernel}")
        res <- q.transact(xa)
      } yield res
  }

}
