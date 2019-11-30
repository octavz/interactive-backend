package com.wantsome
package interactive

import com.dimafeng.testcontainers._
import zio._
import zio.test.Assertion._
import zio.test._
import common.db.migration

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
}
import com.wantsome.interactive.ZIOUtils._

object UserRepoSpec
    extends DefaultRunnableSpec(
      suite("UserRepo")(
        testM("correctly saves the user") {
          for {
            c <- ZIO.access[PostgreSQLContainer](_.container)
            uuid <- ZIO.effect(java.util.UUID.randomUUID().toString)
            r <- migration.migrate(schema = uuid, jdbcUrl = c.getJdbcUrl)
          } yield assert(r, equalTo(1))
        } ,
        testM("correctly saves the user second time") {
          for {
            c <- ZIO.access[PostgreSQLContainer](_.container)
            uuid <- ZIO.effect(java.util.UUID.randomUUID().toString)
            r <- migration.migrate(schema = uuid, jdbcUrl = c.getJdbcUrl)
          } yield assert(r, equalTo(1))
        }
      ).provideManagedShared(Managed.make(setup)(tearDown))
    )
