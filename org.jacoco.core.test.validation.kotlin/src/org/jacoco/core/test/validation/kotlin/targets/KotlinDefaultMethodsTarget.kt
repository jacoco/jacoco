/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

/**
 * This test target contains class implementing interface with default methods.
 */
object KotlinDefaultMethodsTarget {

    interface I { // assertEmpty()
        fun m1() = Unit // assertNotCovered()
        fun m2() = Unit // assertFullyCovered()
        fun m3() = Unit // assertNotCovered()
    } // assertEmpty()

    class C : I { // assertFullyCovered()
        override fun m1() = Unit // assertFullyCovered()
    } // assertEmpty()

    @JvmStatic
    fun main(args: Array<String>) {
        C().m1()
        C().m2()
    }

}
