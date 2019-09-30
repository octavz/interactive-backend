package com.wantsome

package commons

package logger

import zio._

trait Logger {
  val logger: Logger.Service
}

object Logger {

  trait Service {
    def error(message: => String): UIO[Unit]

    def warn(message: => String): UIO[Unit]

    def info(message: => String): UIO[Unit]

    def debug(message: => String): UIO[Unit]

    def trace(message: => String): UIO[Unit]

    def error(t: Throwable)(message: => String): UIO[Unit]

    def warn(t: Throwable)(message: => String): UIO[Unit]

    def info(t: Throwable)(message: => String): UIO[Unit]

    def debug(t: Throwable)(message: => String): UIO[Unit]

    def trace(t: Throwable)(message: => String): UIO[Unit]
  }
}

trait LiveLogger extends Logger {

  val logger: Logger.Service = new Logger.Service {
    def error(message: => String): UIO[Unit] = ???

    def warn(message: => String): UIO[Unit] = ???

    def info(message: => String): UIO[Unit] = ???

    def debug(message: => String): UIO[Unit] = ???

    def trace(message: => String): UIO[Unit] = ???

    def error(t: Throwable)(message: => String): UIO[Unit] = ???

    def warn(t: Throwable)(message: => String): UIO[Unit] = ???

    def info(t: Throwable)(message: => String): UIO[Unit] = ???

    def debug(t: Throwable)(message: => String): UIO[Unit] = ???

    def trace(t: Throwable)(message: => String): UIO[Unit] = ???
  }
}

object LiveLogger extends LiveLogger

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
