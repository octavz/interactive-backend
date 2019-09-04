package com.wantsome.interactive.module.logger

import zio._

object ConsoleLogger extends Logger.Service {
  def error(message: => String) = UIO.effectTotal(println(message))

  def warn(message: => String) = UIO.effectTotal(println(message))

  def info(message: => String) = UIO.effectTotal(println(message))

  def debug(message: => String) = UIO.effectTotal(println(message))

  def trace(message: => String) = UIO.effectTotal(println(message))

  def error(t: Throwable)(message: => String) = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }

  def warn(t: Throwable)(message: => String) = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }

  def info(t: Throwable)(message: => String) = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }

  def debug(t: Throwable)(message: => String) = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }

  def trace(t: Throwable)(message: => String) = UIO.effectTotal {
    t.printStackTrace()
    println(message)
  }
}
