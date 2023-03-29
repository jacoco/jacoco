/*
 * Copyright 2020 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.commons.lang

/**
  * Utility object that defines extended type constraints to be used in Scala type definitions
  */
object TypeConstraints
  extends TypeNegationConstraint


/**
  * Defines `not` type constraint.
  * Can be used to declare a generic type that is _not_ a given type.
  * <br>
  * Inspired by [[https://gist.github.com/milessabin/c9f8befa932d98dcc7a4}]]
  * {{{
  * trait VegMenu {
  *   def add[A <: Food : not[Meat]#λ](food: A)
  * }
  * }}}
  */
trait TypeNegationConstraint {

  // Encoding for "A is not a subtype of B"
  trait !<:[A, B]

  type not[T] = {
    type λ[U] = U !<: T //NOSONAR
    type lambda[U] = λ[U] // ASCII alias for λ
  }

  // use ambiguous method declarations to rule out excluding type conditions
  implicit def passingProbe[A, B]: A !<: B = null //NOSONAR

  implicit def failingProbe1[A, B >: A]: A !<: B = null //NOSONAR

  implicit def failingProbe2[A, B >: A]: A !<: B = null //NOSONAR
}
