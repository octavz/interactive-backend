package com.wantsome.commons.models

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import java.time._
import cats._
import cats.data._

case class User(
  email: String,
  firstName: String,
  lastName: String,
  birthday: LocalDate,
  city: String,
  phone: String,
  occupation: String,
  field: String,
  englishLevel: String,
  itExperience: Boolean,
  experienceDescription: Option[String],
  heardFrom: Option[String])

object User {
  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make[User](CodecMakerConfig())
}

trait FSM {
  sealed trait UserState
   trait NotRegistered extends  UserState
   trait Registered extends  UserState
   trait Tested extends  UserState
   trait TestEvaluated extends  UserState
   trait PassedHR extends  UserState
   trait Refused extends  UserState

  type InitState[T] = IndexedState[NotRegistered, NotRegistered, T]
  type RegisteredState[T] = IndexedState[NotRegistered, Registered, T]
  type TestedState[T] = IndexedState[Registered, Tested, T]
  type TestEvaluatedState[T] = IndexedState[Tested, TestEvaluated, T]
  type PassedHRState[T] = IndexedState[TestEvaluated, PassedHR, T]
  type RefusedState[T] = IndexedState[TestEvaluated, Refused, T]

  def setState[S <: UserState](s: S, u: User) =
       IndexedStateT.set(s)(Applicative[Eval])

}



