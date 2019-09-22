package com.wantsome.interactive

import com.dimafeng.testcontainers._
import org.flywaydb.core.Flyway
import zio._
import zio.test.Assertion._
import zio.test._

object Migration {
  val pgUserName = "postgres"
  val pgPassword = "postgres"

  def migrate(
    schema: String,
    jdbcUrl: String = "jdbc:postgresql://localhost:5432/postgres",
    user: String = pgUserName,
    password: String = pgPassword): Task[Int] =
    ZIO.effect {
      Flyway
        .configure()
        .dataSource(jdbcUrl, user, password)
        .schemas(schema)
        .load()
        .migrate()
    }

  val setup: IO[TestFailure[Throwable], PostgreSQLContainer] =
    ZIO.effect {
      val c = new PostgreSQLContainer(
        dockerImageNameOverride = Some("postgres:11.2"),
        pgUsername = Some(pgUserName),
        pgPassword = Some(pgPassword))
      c.start()
      c
    }.mapError(t => TestFailure.Runtime(Cause.die(t)))

  def tearDown(c: PostgreSQLContainer): UIO[Unit] =
    ZIO
      .effect(c.close())
      .catchAllCause(c => ZIO.effectTotal(System.err.println(c.prettyPrint)) *> ZIO.interrupt)

}

object ZIOUtils {

  def should[R](label: String)(assertion: RIO[R, TestResult]) =
    testM(label) {
      assertion.either map {
        case Left(e) =>
          assert("", failWithException(e))
        case Right(a) => a
      }

    }

  def failWithException(ex: Throwable): Assertion[Any] = {
    val sep = s"\n${" " * 8}"
    Assertion.assertionRec[Any](s"\n${" " * 6}${ex.getMessage}:$sep${ex.getStackTrace.map(_.toString).mkString(sep)}") {
      (self, actual) =>
        AssertResult.failure(AssertionValue(self, actual))
    }
  }

}
import com.wantsome.interactive.ZIOUtils._

object UserRepoSpec
    extends DefaultRunnableSpec(
      suite("UserRepo")(
        should("correctly saves the user") {
          for {
            c <- ZIO.access[PostgreSQLContainer](_.container)
            uuid <- ZIO.effect(java.util.UUID.randomUUID().toString)
            r <- Migration.migrate(schema = uuid, jdbcUrl = c.getJdbcUrl)
          } yield assert(r, equalTo(0))
        },
        should("correctly saves the user") {
          for {
            c <- ZIO.access[PostgreSQLContainer](_.container)
            uuid <- ZIO.effect(java.util.UUID.randomUUID().toString)
            r <- Migration.migrate(schema = uuid, jdbcUrl = c.getJdbcUrl)
          } yield assert(r, equalTo(0))
        }
      ).mapTest(_.provideManaged(Managed.make(Migration.setup)(Migration.tearDown)))
    )
