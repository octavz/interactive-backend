package com.wantsome.interactive

import zio._
import com.dimafeng.testcontainers._

object docker {

  type Container = Has[PostgreSQLContainer]

  val container: URIO[Container, PostgreSQLContainer] =
    ZIO.access[Container](_.get)

  val live: ULayer[Container] = ZLayer.fromManaged {
    def setupContainer =
      ZIO {
        val c = new PostgreSQLContainer(
          dockerImageNameOverride = Some("postgres:11.2"),
          pgUsername = Some("postgres"),
          pgPassword = Some("postgres")
        )
        c.start()
        println(s"container ${c.container.getContainerId} started")
        c
      }.orDie

    def tearDownContainer(container: PostgreSQLContainer) =
      ZIO.effectTotal(println("killing container")) *> ZIO(
        container.container.close()
      ).orDie

    Managed.make(setupContainer)(tearDownContainer)
  }

}
