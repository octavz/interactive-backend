package com.wantsome

package interactive

import java.sql.Timestamp

import io.circe.Encoder
import io.circe.Decoder
import io.circe.HCursor
import io.circe.Json

object implicits {

  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp]
  with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Decoder.Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }

}
