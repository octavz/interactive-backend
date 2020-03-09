package com.wantsome.verifyr.auth
import zio._

object email {
  type EmailBackend = Has[EmailBackend.Service]

  object EmailBackend {

    trait Service {
      def send(email: String): Task[Unit]
    }
  }
}
