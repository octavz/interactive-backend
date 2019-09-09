package com.wantsome

package commons

package models

import java.time._

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
