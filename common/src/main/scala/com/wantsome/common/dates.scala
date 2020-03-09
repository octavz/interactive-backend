package com.wantsome.common

import java.sql.Timestamp

import zio._
import java.time._

import com.wantsome.common.dates.Dates

package object dates {
  type Dates = Has[Dates.Service]

  object Dates {

    trait Service {
      def systemDefaultZone: UIO[ZoneId]
      def now(zoneId: ZoneId): UIO[LocalDateTime]
      def toTimestamp(date: LocalDateTime): UIO[Timestamp]

      def now: UIO[LocalDateTime] = systemDefaultZone >>= now
      def nowUTC: UIO[LocalDateTime] = now(ZoneId.of("UTC"))
      def nowTS: UIO[Timestamp] = nowUTC >>= toTimestamp
    }

    def systemDefaultZone =
      ZIO.accessM[Dates](_.get.systemDefaultZone)

    def now =
      ZIO.accessM[Dates](_.get.now)

    def nowUTC =
      ZIO.accessM[Dates](_.get.nowUTC)

    def now(zoneId: ZoneId) =
      ZIO.accessM[Dates](_.get.now(zoneId))

    def toTimestamp(date: LocalDateTime) =
      ZIO.accessM[Dates](_.get.toTimestamp(date))

    def nowTS =
      ZIO.accessM[Dates](_.get.nowTS)

    val live: ZLayer.NoDeps[Nothing, Dates] = ZLayer.succeed(new LiveDates {})
  }

}

trait LiveDates extends Dates.Service {
  override val systemDefaultZone = ZIO.effectTotal(ZoneId.systemDefault())
  override def now(zoneId: ZoneId) =
    ZIO.effectTotal(LocalDateTime.now(zoneId))
  override def toTimestamp(date: LocalDateTime) =
    ZIO.effectTotal(Timestamp.valueOf(date))
}
