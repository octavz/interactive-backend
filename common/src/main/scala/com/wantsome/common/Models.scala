package com.wantsome

package common

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.{MaxSize, NonEmpty}
import java.sql.Timestamp

private[common] trait Models {

  type DbString = String Refined (NonEmpty And MaxSize[W.`200`.T])
  type Id = String
  type ComboId = Short

  case class ComboValue(id: ComboId, value: DbString, label: DbString)
  case class Combo(values: List[ComboValue])

  sealed trait ComboType
  case object EnglishLevel extends ComboType
  case object Occupation extends ComboType
  case object FieldOfWork extends ComboType

  case class User(
    id: Option[Id],
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
    heardFrom: String)
}
