package com.wantsome.interactive.model.response

sealed trait ErrorResponse extends Product with Serializable
case class InternalServerErrorResponse(message: String, exceptionMessage: String, exception: String)
    extends ErrorResponse
case class NotFoundResponse(message: String) extends ErrorResponse
