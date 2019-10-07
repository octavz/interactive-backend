package com.wantsome
package interactive

import com.dimafeng.testcontainers._
import zio._
import zio.test.Assertion._
import zio.test._
import commons.db.migration

object ZIOUtils {
  val setup: IO[TestFailure[Throwable], PostgreSQLContainer] =
    ZIO.effect {
      val c = new PostgreSQLContainer(
        dockerImageNameOverride = Some("postgres:11.2"),
        pgUsername = Some("postgres"),
        pgPassword = Some("postgres"))
      c.start()
      c
    }.mapError(t => TestFailure.Runtime(Cause.die(t)))

  def tearDown(c: PostgreSQLContainer): UIO[Unit] =
    ZIO
      .effect(c.close())
      .catchAllCause(c => ZIO.effectTotal(System.err.println(c.prettyPrint)) *> ZIO.interrupt)

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
            r <- migration.migrate(schema = uuid, jdbcUrl = c.getJdbcUrl)
          } yield assert(r, equalTo(0))
        },
        should("correctly saves the user") {
          for {
            c <- ZIO.access[PostgreSQLContainer](_.container)
            uuid <- ZIO.effect(java.util.UUID.randomUUID().toString)
            r <- migration.migrate(schema = uuid, jdbcUrl = c.getJdbcUrl)
          } yield assert(r, equalTo(0))
        }
      ).mapTest(_.provideManaged(Managed.make(setup)(tearDown)))
    )
