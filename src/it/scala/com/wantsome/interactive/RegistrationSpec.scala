package com.wantsome

package interactive

import zio._
import zio.test._
import zio.test.Assertion._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.refined.implicits._

import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection._
import com.github.mlangc.slf4zio.api._

import com.wantsome.interactive.ZIOUtils._
import com.wantsome.common.data._
import com.wantsome.verifyr.auth._
import java.sql.Timestamp
import common.data._
import java.util.UUID
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object RegistrationSpec
    extends DefaultRunnableSpec({

      implicit class DbStringHelper(v: String) {
        def asDb: DbString =
          refineV[DbStringConstraint](v).getOrElse(throw new Exception("Failed to convert to DbString"))
      }

      val anyBoundedString = Gen.int(1, 200).flatMap(Gen.stringN(_)(Gen.alphaNumericChar))
      val genDbString = anyBoundedString.map(_.asDb)
      val anyUUID = Gen.fromEffect { ZIO.effectTotal(UUID.randomUUID()) }
      def genShorts(a: Int*) = Gen.oneOf(Gen.elements(a.map(_.toShort): _*))

      val anyUser =
        for {
          id <- anyUUID.map(_.toString())
          email = s"$id@example.com".asDb
          firstName <- genDbString
          lastName  <- genDbString
          birthday <- Gen
                       .localDateTime(LocalDateTime.of(1900, 10, 27, 0, 0), LocalDateTime.now())
                       .map(d => Timestamp.valueOf(d.truncatedTo(ChronoUnit.DAYS)))
          city                  <- genDbString
          phone                 <- genDbString
          occupation            <- genShorts(1, 2, 3)
          fieldOfWork           <- genShorts(1, 2, 3)
          englishLevel          <- genShorts(1, 2, 3)
          itExperience          <- Gen.boolean
          experienceDescription <- Gen.option(anyBoundedString)
          heardFrom             <- genDbString
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

      object spec extends LoggingSupport {

        def apply() = suite("UserRepo")(
          testM("correctly migrates the database and fills english_level_c") {
            val res = sql"""select id from english_level_c""".query[Int].to[List].zio
            assertM(res, hasSize(equalTo(3)))
          },
          testM("correctly migrates the database and fills occupation_c") {
            val recs = sql"""select id from occupation_c""".query[Int].to[List].zio
            val zio = ZIO.access[TestContext](_.transactor).flatMap(c => logger.infoIO(c.toString)) *> recs
            assertM(zio, hasSize(equalTo(4)))
          },
          testM("correctly migrates the database and fills field_of_work_c") {
            val recs = sql"""select id from field_of_work_c""".query[Int].to[List].zio
            assertM(recs, hasSize(equalTo(13)))
          },
          testM("correctly migrates the database and creates user table") {
            val recs = sql"""select id from users""".query[String].option.zio
            assertM(recs, isSome(equalsIgnoreCase("user0")))
          },
          testM("correctly retrieves groups") {
            val recs = Repo.>.groups
            val expected =
              List(Group(id = "student", description = "student"), Group(id = "admin", description = "admin"))
            assertM(recs, hasSameElements(expected))
          },
          testM("correctly inserts users") {
            checkM(anyUser, Gen.oneOf(Gen.elements("student", "admin"))) {
              (testUser, g) =>
                for {
                  _ <- Repo.>.insertUser(testUser, List(g))
                  actual <- sql"""select id,email,first_name,last_name,birthday,city,phone,occupation,
                                field_of_work,english_level,it_experience,experience_description, heard_from
                                from users where id=${testUser.id}"""
                             .query[User]
                             .unique
                             .zio
                  _ <- sql"""delete from groups_users""".update.run.zio
                  _ <- sql"""delete from users""".update.run.zio
                } yield assert(actual, equalTo(testUser))
            }
          }
        )
      }

      spec()
        .provideSomeManaged(TestContext.make)
        .provideManagedShared(ContainerProvider.make)
    })
