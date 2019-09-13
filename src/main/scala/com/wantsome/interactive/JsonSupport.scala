package com.wantsome.interactive

import com.wantsome.commons.models.User
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

trait JsonSupport {
  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make[User](CodecMakerConfig())

}
