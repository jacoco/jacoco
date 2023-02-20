/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.scala.targets

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for synchronized block.
 */
object ScalaSynchronizedTarget {

  private val lock: Object = new Object()

  def main(args: Array[String]): Unit = {
    nop()
    lock.synchronized { // assertFullyCovered()
      /* Without filter next line covered partly */
      nop() // assertFullyCovered()
    } // assertEmpty()
    nop()
  }

}
