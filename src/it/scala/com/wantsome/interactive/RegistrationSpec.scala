package com.wantsome.interactive

import java.sql.Timestamp
import java.util.UUID
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.wantsome.common.config.SettingsProvider
import com.wantsome.common.{data, migration}
import zio._
import zio.random._
import zio.test._
import zio.test.Assertion._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.refined.implicits._
import doobie.util.ExecutionContexts
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection._

import scala.concurrent._
import com.wantsome.common.data._
import com.wantsome.common.db._
import com.wantsome.interactive.docker.Container
import com.wantsome.interactive.util._
import com.wantsome.verifyr.sql.StoreBackend
import com.wantsome.verifyr.store.Repo
import zio.logging.Logging
import zio.logging.Logging.Logging

object RegistrationSpec extends DefaultRunnableSpec {

  implicit class DbStringHelper(v: String) {
    def asDb: DbString =
      refineV[DbStringConstraint](v)
        .getOrElse(throw new Exception("Failed to convert to DbString"))
  }

  val anyBoundedString =
    Gen.int(1, 200).flatMap(Gen.stringN(_)(Gen.alphaNumericChar))
  val genDbString = anyBoundedString.map(_.asDb)
  val anyUUID = Gen.fromEffect { ZIO.effectTotal(UUID.randomUUID()) }
  def genShorts(a: Int*) = Gen.oneOf(Gen.elements(a.map(_.toShort): _*))

  val anyUser =
    for {
      id <- anyUUID.map(_.toString())
      email = s"$id@example.com".asDb
      firstName <- genDbString
      lastName <- genDbString
      birthday <- Gen
        .localDateTime(
          LocalDateTime.of(1900, 10, 27, 0, 0),
          LocalDateTime.now()
        )
        .map(d => Timestamp.valueOf(d.truncatedTo(ChronoUnit.DAYS)))
      city <- genDbString
      phone <- genDbString
      occupation <- genShorts(1, 2, 3)
      fieldOfWork <- genShorts(1, 2, 3)
      englishLevel <- genShorts(1, 2, 3)
      itExperience <- Gen.boolean
      experienceDescription <- Gen.option(anyBoundedString)
      heardFrom <- genDbString
    } yield User(
      id = id,
      email = email,
      firstName = firstName,
      lastName = lastName,
      birthday = birthday,
      city = city,
      phone = phone,
      occupation = occupation,
      fieldOfWork = fieldOfWork,
      englishLevel = englishLevel,
      itExperience = itExperience,
      experienceDescription = experienceDescription,
      heardFrom = heardFrom
    )

  def dbConfig =
    (for {
      cont <- ZIO.access[Container](_.get)
      uuid <- ZIO.effectTotal(
        java.util.UUID.randomUUID.toString.replace("-", "")
      )
      jdbcUrl <- ZIO(cont.container.getJdbcUrl).map(u =>
        s"$u&currentSchema=$uuid"
      )
      res <- ZIO(
        DatabaseConfig(
          schema = refineV[NonEmpty](uuid)
            .getOrElse(throw new Exception("Schema is not valid")),
          user = "postgres",
          password = "postgres",
          className = "org.postgresql.Driver",
          url = refineV[NonEmpty](jdbcUrl)
            .getOrElse(throw new Exception("Jdbc url not valid"))
        )
      )
    } yield res).orDie

  val transactorL: URLayer[Container, TransactorProvider] = {
    val cl: URLayer[Container, Has[DatabaseConfig]] =
      ZLayer.fromEffect(dbConfig)

    val tl: URLayer[Logging with SettingsProvider with Has[data.DatabaseConfig], TransactorProvider] =
      ZLayer.fromManaged {
        (for {
          c <- ZIO.access[Has[DatabaseConfig]](_.get).toManaged_
          t <- mkTransactor(
            c,
            ExecutionContext.Implicits.global,
            ExecutionContexts.synchronous
          )
          _ <- migration.migrate.toManaged_
        } yield new TransactorProvider.Service {
          override val transactor: Transactor[Task] = t
        }).orDie
      }

    val sl: URLayer[Has[DatabaseConfig], SettingsProvider] =
      ZLayer.fromService((c: DatabaseConfig) =>
        new SettingsProvider.Service {
          override val config: Task[AppConfig] =
            Task.succeed(AppConfig(3600, c))
        }
      )

    val ll: ULayer[Logging] =
      (console.Console.live ++ clock.Clock.live) >>> Logging.console((_, s) =>
        s
      )
    ( ( cl >>> sl) ++ ll ++ cl) >>> tl
  }

  def spec =
    suite("UserRepo")(
      testM("correctly migrates the database and fills english_level_c") {
        val res =
          sql"""select id from english_level_c""".query[Int].to[List].zio
        assertM(res)(hasSize(equalTo(3)))
      },
      testM("correctly migrates the database and fills occupation_c") {
        val recs =
          sql"""select id from occupation_c""".query[Int].to[List].zio
        assertM(recs)(hasSize(equalTo(4)))
      },
      testM("correctly migrates the database and fills field_of_work_c") {
        val recs =
          sql"""select id from field_of_work_c""".query[Int].to[List].zio
        assertM(recs)(hasSize(equalTo(13)))
      },
      testM("correctly migrates the database and creates user table") {
        val recs = sql"""select id from users""".query[String].option.zio
        assertM(recs)(isSome(equalsIgnoreCase("user0")))
      },
      testM("correctly retrieves groups") {
        val recs = Repo.groups
        val expected =
          List(
            Group(id = "student", description = "student"),
            Group(id = "admin", description = "admin")
          )

        assertM(recs)(hasSameElements(expected))
      },
      testM("correctly inserts users") {
        checkM(anyUser, Gen.oneOf(Gen.elements("student", "admin"))) {
          (testUser, g) =>
            for {
              _ <- Repo.insertUser(testUser, List(g))
              actual <- sql"""select id,email,first_name,last_name,birthday,city,phone,occupation,
                                field_of_work,english_level,it_experience,experience_description, heard_from
                                from users where id=${testUser.id}"""
                .query[User]
                .unique
                .zio
            } yield assert(actual)(equalTo(testUser))
        }
      }
    ).provideSomeLayer[Container with Random](
        transactorL ++ ((StoreBackend.live ++ transactorL) >>> Repo.live)
      ).provideLayerShared(
        docker.live ++ random.Random.live
      )

}
