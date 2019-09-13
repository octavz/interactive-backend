package com.wantsome.interactive.finch

import java.nio.charset.StandardCharsets

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromByteBuffer, readFromString, writeToString}
import com.twitter.io.Buf
import io.finch.internal.Utf32
import io.finch.{Decode, Encode}

import scala.util.Try

object JsonIter {

  /**
    * @param reads json-iter codec to use for decoding
    * @tparam A the type of the data to decode into
    */
  implicit def decodePlayJson[A](implicit reads: JsonValueCodec[A]): Decode.Json[A] =
    Decode.json {
      case (buf, StandardCharsets.UTF_8 | StandardCharsets.UTF_16 | Utf32) =>
        Try(readFromByteBuffer(buf.asByteBuffer): A).fold(Left.apply, Right.apply)
      case (buf, cs) =>
        Try(readFromString(buf.asString(cs)): A).fold(Left.apply, Right.apply)
    }

  /**
    * @param writes json-iter codec to use for encoding
    * @tparam A the type of the data to encode from
    */
  implicit def encodePlayJson[A](implicit writes: JsonValueCodec[A]): Encode.Json[A] =
    Encode.json((a, cs) => Buf.ByteArray.Owned(writeToString(a).getBytes(cs.name)))

}
