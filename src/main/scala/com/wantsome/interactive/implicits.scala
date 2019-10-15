package com.wantsome

package interactive

import java.sql.Timestamp

import io.circe.Encoder
import com.twitter.io.Buf
import io.circe.Decoder
import io.circe.HCursor
import io.finch.Application
import io.finch.Encode
import io.circe.Json

object implicits {

  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp]
  with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Decoder.Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }
  implicit val e: Encode.Aux[Exception, Application.Json] =
    Encode.instance((e, _) => Buf.Utf8(s"""{"error": "Bad thing happened: ${e.getMessage}}""""))

  def encodeErrorList(es: List[Exception]) = {
    val messages = es.map(x => Json.fromString(x.getMessage))
    Json.obj("errors" -> Json.arr(messages: _*))
  }

  implicit val encodeException: Encoder[Exception] = Encoder.instance({
    case e: io.finch.Errors => encodeErrorList(e.errors.toList)
    case e: io.finch.Error =>
      e.getCause match {
        case e: io.circe.Errors => encodeErrorList(e.errors.toList)
        case _ => Json.obj("message" -> Json.fromString(e.getMessage))
      }
    case e: Exception => Json.obj("message" -> Json.fromString(e.getMessage))
  })

}
