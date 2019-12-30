package com.wantsome.interactive


import com.dimafeng.testcontainers._

import zio._
import zio.console._
import zio.interop.catz._

import zio.test._
import zio.test.Assertion._

import doobie._
import doobie.implicits._
import cats.effect.Blocker

import com.wantsome.common.DatabaseConfig

import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty

import scala.concurrent.ExecutionContext
import com.zaxxer.hikari.HikariDataSource
import doobie.hikari.HikariTransactor

import org.flywaydb.core.Flyway

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
