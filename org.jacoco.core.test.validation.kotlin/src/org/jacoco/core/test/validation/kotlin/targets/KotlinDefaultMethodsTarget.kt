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
package org.jacoco.core.test.validation.kotlin.targets

/**
 * This test target contains class implementing interface with default methods.
 */
object KotlinDefaultMethodsTarget {

    interface I {
        fun m1() = Unit // assertNotCovered()
        fun m2() = Unit // assertFullyCovered()
        fun m3() = Unit // assertNotCovered()
    }

    class C : I { // assertFullyCovered()
        override fun m1() = Unit // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        C().m1()
        C().m2()
    }

}
