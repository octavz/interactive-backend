package com.wantsome

package commons

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.{MaxSize, NonEmpty}

object models {

  type DbString = String Refined (NonEmpty And MaxSize[W.`200`.T])
  type Id = String
  type ComboId = Short

  case class ComboValue(id: ComboId, value: DbString, label: DbString)
  case class Combo(values: List[ComboValue])
}
