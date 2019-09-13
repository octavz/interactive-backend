package com.wantsome
package verifyr

import io.finch._
import org.scalatest.FunSuite
import zio.DefaultRuntime
import zio.interop.catz._

class MainTest extends FunSuite with DefaultRuntime{

  implicit  val runtime = this

  test("healthcheck") {
    assert(Main.healthcheck(Input.get("/")).awaitValueUnsafe() == Some("OK"))
  }

  test("helloWorld") {
    assert(Main.helloWorld(Input.get("/hello"))
      .awaitValueUnsafe() == Some(Main.Message("World")))
  }

  test("hello") {
    assert(Main.hello(Input.get("/hello/foo"))
      .awaitValueUnsafe() == Some(Main.Message("foo")))
  }
}