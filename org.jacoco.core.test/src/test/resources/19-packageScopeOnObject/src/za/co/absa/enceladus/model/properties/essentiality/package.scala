/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.enceladus.model.properties

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

package object essentiality {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_t")
  @JsonSubTypes(Array(
    new Type(value = classOf[Optional], name = "Optional"),
    new Type(value = classOf[Recommended], name = "Recommended"),
    new Type(value = classOf[Mandatory], name = "Mandatory")
  ))
  sealed trait Essentiality

  case class Optional() extends Essentiality

  case class Recommended() extends Essentiality

  case class Mandatory(allowRun: Boolean = false) extends Essentiality

  object Essentiality {
    final val Optional: Optional = new Optional() //the new have to be used despite calling case class, because of ambiguity
    final val Recommended: Recommended = new Recommended()
    //scalastyle:off method.name - to be able to use the case class as case object
    final def Mandatory(allowRun: Boolean): Mandatory = new Mandatory(allowRun)
    //scalastyle:on method.name
    final val Mandatory: Mandatory = new Mandatory()
  }
}
