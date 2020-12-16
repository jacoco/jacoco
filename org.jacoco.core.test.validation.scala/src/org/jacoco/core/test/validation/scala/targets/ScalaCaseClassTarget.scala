/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Gergely Fábián - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.scala.targets

import org.jacoco.core.test.validation.targets.Stubs.{exec, noexec, nop}

/**
 * Test target for anonymous functions.
 */
object ScalaCaseClassTarget {

  case class Foo(a: Int, b: String, c: Set[String], d: Double) // assertPartlyCovered(31, 0)

  def main(args: Array[String]): Unit = {

    val foo = Foo(1, "test", Set.empty[String], 2.3) // assertFullyCovered()
    val message = "" + foo.a + foo.b + foo.c.mkString(",") + foo.d // assertFullyCovered()

  }

}
