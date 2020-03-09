package com.wantsome.interactive

import zio.{test => _, _}
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import doobie._

import scala.concurrent.ExecutionContext
import com.wantsome.common.db.migration
import com.wantsome.verifyr.auth._
import com.wantsome.common.TransactorProvider
import com.wantsome.common.config.DatabaseConfig
import zio.random._

trait TestContext {
  val transactor: Transactor[Task]
}

object TestContext {

  def testTransactor(config: DatabaseConfig): Managed[Throwable, Transactor[Task]] =
    com.wantsome.common.db.transactor
      .mkTransactor(config, ExecutionContext.Implicits.global, ExecutionContexts.synchronous)

  def make: ZManaged[ContainerProvider, Nothing, TestContext with ContainerProvider with LiveRepo with Random] =
    (for {
      cont    <- ZIO.access[ContainerProvider](_.container).toManaged_
      _       <- ZIO.effectTotal(println(s"seting up transactor for ${cont.container.getContainerId}")).toManaged_
      uuid    <- ZIO(java.util.UUID.randomUUID.toString.replace("-", "")).map(u => s"schema$u").toManaged_
      jdbcUrl <- ZIO(cont.container.getJdbcUrl).map(u => s"$u&currentSchema=$uuid").toManaged_
      _       <- migration.migrate(schema = uuid, jdbcUrl = jdbcUrl).orDie.toManaged_
      c <- ZIO(
            DatabaseConfig(
              schema = refineV[NonEmpty](uuid).getOrElse(throw new Exception("Schema is not valid")),
              user = "postgres",
              password = "postgres",
              className = "org.postgresql.Driver",
              url = refineV[NonEmpty](jdbcUrl).getOrElse(throw new Exception("Jdbc url not valid"))
            )).toManaged_
      _  <- ZIO.effectTotal(println(s"Created connection for $jdbcUrl")).toManaged_
      tr <- testTransactor(c)
    } yield new ContainerProvider with TestContext with LiveRepo with Random.Live {
      override val container = cont
      override val transactor = tr
      override val transactorProvider = new TransactorProvider {
        override val transactor = tr
      }
    }).orDie

}
