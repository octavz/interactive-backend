package com.wantsome.common

import java.sql.Timestamp

import zio._
import java.time._

import zio.macros.annotation.accessible

@accessible
trait Dates {
  val dates: Dates.Service[Any]
}

object Dates {

  trait Service[R] {
    def systemDefaultZone: URIO[R, ZoneId]
    def now: ZIO[R, Nothing, LocalDateTime] = systemDefaultZone >>= now
    def nowUTC: ZIO[R, Nothing, LocalDateTime] = now(ZoneId.of("UTC"))
    def now(zoneId: ZoneId): ZIO[R, Nothing, LocalDateTime]
    def toTimestamp(date: LocalDateTime): ZIO[R, Nothing, Timestamp]

    def nowTS: ZIO[R, Nothing, Timestamp] =
      nowUTC >>= toTimestamp
  }
}

package object dates extends Dates.Accessors

trait LiveDates extends Dates {

  override val dates = new Dates.Service[Any] {
    override val systemDefaultZone = ZIO.effectTotal(ZoneId.systemDefault())
    override def now(zoneId: ZoneId): UIO[LocalDateTime] =
      ZIO.effectTotal(LocalDateTime.now(zoneId))
    override def toTimestamp(date: LocalDateTime): UIO[Timestamp] =
      ZIO.effectTotal(Timestamp.valueOf(date))
  }

}
