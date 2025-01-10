/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.kotlin.targets

/**
 * Test target with synthetic accessor methods.
 */
object KotlinSyntheticAccessorsTarget {

    class Outer { // assertFullyCovered()
        private var x = 42

        private fun getX(): Int {
            return x
        }

        inner class Inner { // assertFullyCovered()
            /**
             * Access to private field and function of outer class causes creation of synthetic methods in it.
             * Those methods refer to the line of outer class definition.
             */
            @Suppress("unused")
            fun accessOuter() {
                x += 1 // assertNotCovered()
                getX()
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Outer().Inner()
    }

}
