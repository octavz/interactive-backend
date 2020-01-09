package com.wantsome

package common

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.{MaxSize, NonEmpty}
import java.sql.Timestamp

private[common] trait Models {

  type DbStringConstraint = NonEmpty And MaxSize[W.`200`.T]
  type DbString = String Refined DbStringConstraint
  type Id = String
  type ComboId = Short

  case class ComboValue(id: ComboId, value: DbString, label: DbString)
  case class Combo(values: List[ComboValue])

  sealed trait ComboType
  case object EnglishLevel extends ComboType
  case object Occupation extends ComboType
  case object FieldOfWork extends ComboType

  case class User(
    id: Id,
    email: DbString,
    firstName: DbString,
    lastName: DbString,
    birthday: Timestamp,
    city: DbString,
    phone: DbString,
    occupation: Short,
    fieldOfWork: Short,
    englishLevel: Short,
    itExperience: Boolean,
    experienceDescription: Option[String],
    heardFrom: String
  )

  case class Group(id: Id, description: String)
  case class UserGroup(userId: Id, groupId: Id)

  case class Invitation(id: Id, userId: Id, expiresAt: Timestamp)

  case class Quiz(id: Id, userId: Id, description: String)
  case class QuestionSet(id: Id, quizzId: Id, title: String)
  case class Question(id: Id, questionSetId: Id, content: String)
  case class Answer(id: Id, questionId: Id, content: String, isCorrect: Boolean)

}
