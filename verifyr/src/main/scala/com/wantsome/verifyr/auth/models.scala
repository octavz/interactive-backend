package com.wantsome

package verifyr

package auth

import java.sql.Timestamp
import commons.models._

object models {

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
