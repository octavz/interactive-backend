package com.wantsome.verifyr
package repository

import org.flywaydb.core.Flyway
import zio.test._
import zio.test.Assertion._
import zio._
import com.dimafeng.testcontainers._

object Migration {

  def migrate(
    schema: String,
    host: String = "localhost",
    port: Int = 5432,
    user: String = "postgres",
    password: String = "postgres") =
    ZIO.effect {
      Flyway
        .configure()
        .dataSource(s"jdbc:postgresql://$host:$port/postgres", user, password)
        .schemas(schema)
        .load()
        .migrate()
    }
  val docker = ZManaged.make(ZIO.effect(new PostgreSQLContainer(Some("postgres:11.2"))))(container
  => ZIO.effectTotal(try{container.close()}))
}

object UserRepoSpec
    extends DefaultRunnableSpec(
      suite("UserRepo")(
        testM("correctly saves the user") {
          val test = for {
            _ <- ZIO.effect(new PostgreSQLContainer(Some("postgres:11.2")))
            uuid <- ZIO.effect(java.util.UUID.randomUUID().toString)
            r <- Migration.migrate(uuid)
          } yield r
          test.either map (assert(_, isRight(equalTo(1))))
        }
      ) @@ TestAspect.around(Migration.docker)
    )
