package com.wantsome.interactive

import zio.{test => _, _}

import java.sql._
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty

import doobie._
import scala.concurrent.ExecutionContext

import com.wantsome.common.db.migration
import com.wantsome.common.DatabaseConfig
import com.wantsome.verifyr.auth._
import com.wantsome.common.TransactorProvider

trait TestContext {
  val config: DatabaseConfig
  def conn: Connection
  val transactor: Transactor[Task]

  def testUser =
    for {
      conn <- ZIO.access[TestContext](_.conn)
      res <- ZIO {
        val rs = conn.createStatement.executeQuery(s"select id,email from users_")
        rs.next()
        (rs.getString("id"), rs.getString("email"))
      }
    } yield res
}

object TestContext {
  def testTransactor(config: DatabaseConfig): Managed[Throwable, Transactor[Task]] = 
    com.wantsome.common.db.transactor.mkTransactor(config,
      ExecutionContext.Implicits.global,
      ExecutionContexts.synchronous)

  def make: ZManaged[ContainerProvider, Nothing, TestContext with ContainerProvider with LiveRepo] =
    (for {
      cont <- ZIO.access[ContainerProvider](_.container).toManaged_
      _ <- ZIO.effectTotal( println( s"seting up transactor for ${cont.container.getContainerId}")).toManaged_
      uuid <- ZIO(java.util.UUID.randomUUID.toString.replace("-", "")) .map(u => s"schema$u").toManaged_
      jdbcUrl <- ZIO(cont.container.getJdbcUrl).map(u => s"$u&currentSchema=$uuid").toManaged_
      _ <- migration.migrate(schema = uuid, jdbcUrl = jdbcUrl).orDie.toManaged_
      c <- ZIO(
        DatabaseConfig(
          schema = refineV[NonEmpty](uuid).getOrElse(throw new Exception("Schema is not valid")),
          user = "postgres",
          password = "postgres",
          className = "org.postgresql.Driver",
          url = refineV[NonEmpty](jdbcUrl).getOrElse(throw new Exception("Jdbc url not valid"))
        )).toManaged_
      co <- ZIO {
        DriverManager.getConnection(jdbcUrl, "postgres", "postgres")
      }.toManaged(c => ZIO(c.close).orDie)
      _ <- ZIO.effectTotal(println(s"Created connection for $jdbcUrl")).toManaged_
      tr <- testTransactor(c)
    } yield new ContainerProvider with TestContext with LiveRepo {
      override val conn = co
      override val container = cont
      override val config = c
      override val transactor = tr
      override val transactorProvider = new TransactorProvider{
        override val transactor = tr
      }
    }).orDie

}
