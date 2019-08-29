package com.wantsome.interactive.model

sealed abstract class Error
case class UnexpectedError(cause: Throwable) extends Error
case class DBError(cause: Exception) extends Error
case class NotFoundError(message: String) extends Error
