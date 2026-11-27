/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.scala.targets

/**
 * Test target for the boundary counter. Every method contains the comparisons
 * of exactly one Scala construct, so that the boundary counter of the method is
 * the counter of that construct.
 */
class ScalaBoundaryTarget {

  def greaterThan(arg: Int): Boolean =
    arg > 6

  def greaterOrEqual(arg: Int): Boolean =
    arg >= 6

  def equalTo(arg: Int): Boolean =
    arg == 6

  def matchWithGuard(arg: Int): String =
    arg match {
      case x if x > 6 => "big"
      case _          => "small"
    }

  def twoComparisons(arg: Int): Boolean =
    arg >= 1 && arg <= 10

  def rangeContains(arg: Int): Boolean =
    (1 to 10).contains(arg)

  def longGreaterThan(arg: Long): Boolean =
    arg > 6L

  def doubleLessThan(arg: Double): Boolean =
    arg < 6d

  def stringGreaterThan(a: String, b: String): Boolean =
    a > b

  def stringCompareTo(a: String, b: String): Boolean =
    a.compareTo(b) > 0

  def countUpTo(arg: Int): Int = {
    var sum = 0
    var i = 0
    while (i < arg) {
      sum += i
      i += 1
    }
    sum
  }

}
