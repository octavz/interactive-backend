package com.wantsome

package interactive

import io.circe._
import java.sql._

object json {

  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp]
  with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Decoder.Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }

}
