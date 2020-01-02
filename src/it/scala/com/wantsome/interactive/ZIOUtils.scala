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
      ZIO
        .access[TestContext](_.config)
        .flatMap(c => logger.infoIO(s"Running query with config: $c")) *>
        ZIO.access[TestContext](_.transactor) >>= (q.transact(_))
  }

}
