package com.wantsome.interactive.module.logger

import zio._

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
