package com.wantsome.commons

import cats.data.{IndexedState, IndexedStateT}
import cats.{Applicative, Eval}

object FSM {
  sealed trait UserState
  trait NotRegistered extends UserState
  trait Registered extends UserState
  trait Tested extends UserState
  trait TestEvaluated extends UserState
  trait PassedHR extends UserState
  trait Refused extends UserState

  type InitState[T] = IndexedState[NotRegistered, NotRegistered, T]
  type RegisteredState[T] = IndexedState[NotRegistered, Registered, T]
  type TestedState[T] = IndexedState[Registered, Tested, T]
  type TestEvaluatedState[T] = IndexedState[Tested, TestEvaluated, T]
  type PassedHRState[T] = IndexedState[TestEvaluated, PassedHR, T]
  type RefusedState[T] = IndexedState[TestEvaluated, Refused, T]

  def setState[S <: UserState](s: S): IndexedStateT[Eval, Nothing, S, Unit] =
    IndexedStateT.set(s)(Applicative[Eval])

}
