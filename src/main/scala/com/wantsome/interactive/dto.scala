package com.wantsome

package interactive

import java.sql.Timestamp
import eu.timepit.refined.auto._
import common.data._

object dto {
  case class ComboValueDTO(id: Short, value: String, label: String)

  case class CombosDTO(
      occupation: List[ComboValueDTO],
      fieldOfWork: List[ComboValueDTO],
      englishLevel: List[ComboValueDTO]
  )

  object ComboValueDTO {

    def apply(c: ComboValue): ComboValueDTO =
      ComboValueDTO(id = c.id, value = c.value, label = c.label)
  }

  case class UserDTO(
      id: Option[Id],
      email: String,
      firstName: String,
      lastName: String,
      birthday: Timestamp,
      city: String,
      phone: String,
      occupation: Short,
      fieldOfWork: Short,
      englishLevel: Short,
      itExperience: Boolean,
      experienceDescription: Option[String],
      heardFrom: String
  )

}
