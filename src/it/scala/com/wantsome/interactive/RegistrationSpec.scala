package com.wantsome

package interactive

import zio.{test => _, _}
import zio.test._
import zio.test.Assertion._
import doobie.implicits._

import eu.timepit.refined.auto._
import com.github.mlangc.slf4zio.api._

import com.wantsome.interactive.ZIOUtils._

object RegistrationSpec 
    extends DefaultRunnableSpec ({

      object spec extends LoggingSupport {
        def apply() = suite("UserRepo")(
          testM("correctly migrates the database and fills english_level_c") {
            val res = sql"""select id from english_level_c""".query[Int].to[List].zio
            assertM(res, hasSize(equalTo(3)))
          },
          testM("correctly migrates the database and fills occupation_c") {
            val recs = sql"""select id from occupation_c""".query[Int].to[List].zio
            val zio = ZIO.access[TestContext](_.config).flatMap(c => logger.infoIO(c.toString)) *> recs
            assertM(zio, hasSize(equalTo(4)))
          },
          testM("correctly migrates the database and fills field_of_work_c") {
            val recs = sql"""select id from field_of_work_c""".query[Int].to[List].zio
            assertM(recs, hasSize(equalTo(13)))
          },
          testM("correctly migrates the database and creates user table") {
            val recs = sql"""select id from users""".query[String].option.zio
            assertM(recs, isSome(equalsIgnoreCase("user0")))
          }
        )
      }
      spec()
        .provideSomeManaged(TestContext.make)
        .provideManagedShared(ContainerProvider.make)
    }) 
