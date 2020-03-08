package com.wantsome

package common

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
      ZIO {
        Flyway
          .configure()
          .dataSource(jdbcUrl, user, password)
          .defaultSchema(schema)
          .schemas(schema)
          .load()
          .migrate()
      }
}
