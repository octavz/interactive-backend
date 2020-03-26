package com.wantsome.common

import org.flywaydb.core.Flyway
import zio._
import zio.logging._
import config._

object migration {

  def migrate = SettingsProvider.config flatMap { c =>
    migrateInternal(
      schema = c.database.schema.value,
      jdbcUrl = c.database.url.value,
      user = c.database.user.value,
      password = c.database.password.value
    )
  }
  private[common] def migrateInternal(
      schema: String,
      jdbcUrl: String = "jdbc:postgresql://localhost:5432/postgres",
      user: String = "postgres",
      password: String = "postgres"
  ) =
    log.info(s"Migrating database, for schema: $schema with url: $jdbcUrl") *>
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
