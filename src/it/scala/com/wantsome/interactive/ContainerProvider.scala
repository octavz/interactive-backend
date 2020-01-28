package com.wantsome.interactive

import zio._
import com.dimafeng.testcontainers._

trait ContainerProvider {
  val container: PostgreSQLContainer
}

object ContainerProvider {

  def setupContainer =
    ZIO {
      val c = new PostgreSQLContainer(
        dockerImageNameOverride = Some("postgres:11.2"),
        pgUsername = Some("postgres"),
        pgPassword = Some("postgres"))
      c.start()
      println(s"container ${c.container.getContainerId} started")
      c
    }.map(c =>
        new ContainerProvider {
          override val container = c
        })
      .orDie

  def tearDownContainer(container: ContainerProvider) =
    ZIO.effectTotal(println("killing container")) *> ZIO(container.container.close).orDie

  def make: Managed[Nothing, ContainerProvider] =
    Managed.make(setupContainer)(tearDownContainer(_))

}
