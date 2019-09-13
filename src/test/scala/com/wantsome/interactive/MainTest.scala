package com.wantsome.interactive

import io.finch._
import org.scalatest._

class MainTest extends FlatSpec with Matchers {
  "healthcheck" should "be present" in {
    Main.healthcheck(Input.get("/")).awaitValueUnsafe() shouldBe Some("OK")
  }

  "helloWorld" should "be present" in{
    Main.helloWorld(Input.get("/hello")).awaitValueUnsafe() shouldBe Some(Main.Message("World"))
  }

  it should "implement hello route"  in {
    Main.hello(Input.get("/hello/foo")).awaitValueUnsafe() shouldBe Some(Main.Message("foo"))
  }
}