package com.wantsome

package commons

package models

import db._

case class User(
  id: Option[Id],
  email: DbString,
  firstName: DbString,
  lastName: DbString,
  birthday: String,
  city: DbString,
  phone: DbString,
  occupation: Short,
  fieldOfWork: Short,
  englishLevel: Short,
  itExperience: Boolean,
  experienceDescription: Option[String],
  heardFrom: String)

/*
{ "email":"test@exmaple.com", "firstName":"John", "lastName":"Popescu", "birthday":"1980/31/21",
"city":"Iasi", "phone":"+40742012378", "occupation":5, "fieldOfWork":4, "englishLevel":3,
"itExperience":2, "heardFrom":"Facebook" }
 */
