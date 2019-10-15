package com.wantsome

package interactive

import java.sql.Timestamp
import common.data._

object dto {
  type ComboDTO = List[ComboValue]
  case class CombosDTO(occupation: List[ComboValue], fieldOfWork: List[ComboValue], englishLevel: List[ComboValue])

  case class UserDTO(
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
