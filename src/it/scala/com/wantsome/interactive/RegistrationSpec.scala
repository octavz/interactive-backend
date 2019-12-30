package com.wantsome

package interactive

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

/*
trait TestContext extends Console.Live {
  val config: DatabaseConfig
  val transactor: HikariTransactor[Task]
}

object TestContext {
  def setup: ZIO[ContainerProvider, Nothing, TestContext with ContainerProvider] =
    (for {
      cont <- ZIO.access[ContainerProvider](_.container)
      _ <- ZIO.effectTotal(println(s"seting up transactor for ${cont.container.getContainerId}"))
      uuid <- ZIO(java.util.UUID.randomUUID.toString.replace("-", "")).map(u => s"schema$u")
      jdbcUrl <- ZIO(cont.container.getJdbcUrl).map(u => s"$u&currentSchema=$uuid")
      _ <- migrate(schema = uuid, jdbcUrl = jdbcUrl)
      c <- ZIO(
        DatabaseConfig(
          schema = refineV[NonEmpty](uuid).getOrElse(throw new Exception("Schema is not valid")),
          user = "postgres",
          password = "postgres",
          className = "org.postgresql.Driver",
          url = refineV[NonEmpty](jdbcUrl).getOrElse(throw new Exception("Jdbc url not valid"))
        ))
      tr <- testTransactor(c)
    } yield new TestContext with ContainerProvider {
      override val config = c
      override val transactor = tr
      override val container = cont
    }).orDie

  val make: ZManaged[ContainerProvider, TestFailure[Throwable], TestContext with ContainerProvider] =
    setup.toManaged { c =>
      ZIO.effectTotal(println("killing transactor")) *> ZIO(c.transactor.kernel.getConnection.close).orDie
    }

  def testTransactor(config: DatabaseConfig): Task[HikariTransactor[Task]] = ZIO {
    val ds = new HikariDataSource()
    ds.setDriverClassName(config.className)
    ds.setSchema(config.schema)
    ds.setJdbcUrl(config.url)
    ds.setPassword(config.password)
    ds.setUsername(config.user)
    HikariTransactor[Task](
      ds,
      ExecutionContext.Implicits.global,
      Blocker.liftExecutionContext(ExecutionContexts.synchronous))
  }
}
 */
object ZIOUtils {
  implicit class DoobieHelper[A](q: ConnectionIO[A]) {

    def zio[A] =
      ZIO
        .access[TestContext](_.config)
        .flatMap(c => ZIO.effectTotal(println(s"Running query with config: $c"))) *>
        ZIO.access[TestContext](_.transactor) >>=
        (q.transact(_))
  }

  val mytest = ZIO
    .access[TestContext](_.config)
    .flatMap(c => ZIO.effectTotal(println(c))) *> sql"""select id from english_level_c""".query[Int].to[List].zio

  val mytest1 =  for {
      conn <- ZIO.access[TestContext](_.conn)
      c <- ZIO.access[TestContext](_.config)
      _ <- ZIO.effectTotal(println(c))
      res <- ZIO {
        val rs = conn.createStatement.executeQuery(s"select id from english_level_c")
        val buf = scala.collection.mutable.ListBuffer.empty[Int]
        while(rs.next){
          buf += rs.getInt("id") 
        }
        buf.toList
      }
    } yield res
}

import com.wantsome.interactive.ZIOUtils._

object RegistrationSpec
    extends DefaultRunnableSpec({
      // val spec =
      //   suite("UserRepo")(
      //     testM("correctly migrates the database and fills english_level_c") {
      //       assertM(mytest, hasSize(equalTo(3)))
      //     },
      //     testM("correctly migrates the database and fills english_level_c") {
      //       assertM(mytest, hasSize(equalTo(3)))
      //     }
      //     // testM("correctly migrates the database and fills occupation_c") {
      //     //   val recs = sql"""select id from occupation_c""".query[Int].to[List].zio
      //     //   val zio = ZIO.access[TestContext](_.config).flatMap(c => ZIO.effectTotal(println(c))) *> recs
      //     //   assertM(zio, hasSize(equalTo(4)))
      //     // },
      //     // testM("correctly migrates the database and fills field_of_work_c") {
      //     //   val recs = sql"""select id from field_of_work_c""".query[Int].to[List].zio
      //     //   assertM(recs, hasSize(equalTo(13)))
      //     // },
      //     // testM("correctly migrates the database and creates user table") {
      //     //   val recs = sql"""select id from users""".query[String].option.zio
      //     //   assertM(recs, isNone)
      //     // }
      //   )
      def spec =
        suite("ExampleSpec")(
          testM("first test") {
            assertM(mytest, hasSize(equalTo(3)))
          },
          testM("second test") {
            assertM(mytest1, hasSize(equalTo(3)))
          },
          testM("third test") {
            assertM(mytest1, hasSize(equalTo(3)))
          }
        )
      spec
        .provideSomeManaged(TestContext.make)
        .provideManagedShared(ContainerProvider.make)
    })
