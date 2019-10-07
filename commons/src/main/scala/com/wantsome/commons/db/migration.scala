package com.wantsome

package commons

package db
import org.flywaydb.core.Flyway
import zio._
import com.github.mlangc.slf4zio.api._

object migration extends LoggingSupport {

  def migrate(
    schema: String,
    jdbcUrl: String = "jdbc:postgresql://localhost:5432/postgres",
    user: String = "postgres",
    password: String = "postgres"): Task[Int] =
    logger.infoIO(s"Migrating database, for schema: $schema with url: $jdbcUrl") *>
      ZIO.effect {
        Flyway
          .configure()
          .dataSource(jdbcUrl, user, password)
          .schemas(schema)
          .load()
          .migrate()
      }

}
