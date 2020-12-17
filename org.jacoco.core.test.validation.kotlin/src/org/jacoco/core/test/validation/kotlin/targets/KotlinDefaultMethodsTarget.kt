/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * This test target contains class implementing interface with default methods.
 */
object KotlinDefaultMethodsTarget {

    interface I {
        fun overriddenWithoutSuperCall() = Unit // assertNotCovered()
        fun overridden() = Unit // assertFullyCovered()
        fun overriddenRedundantly() = Unit // assertFullyCovered()
        fun overriddenBySuperCallOfAnotherMethod() = Unit // assertNotCovered()
        fun notOverridden() = Unit // assertFullyCovered()
        fun notOverriddenNotCalled() = Unit // assertNotCovered()
    }

    class C : I { // assertFullyCovered()
        override fun overriddenWithoutSuperCall() = Unit // assertFullyCovered()

        override fun overridden() {
            super.overridden() // assertFullyCovered()
            nop() // assertFullyCovered()
        }

        override fun overriddenRedundantly() {
            super.overriddenRedundantly() // assertEmpty()
        }

        override fun overriddenBySuperCallOfAnotherMethod() {
            super.overridden() // assertFullyCovered()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        C().overriddenWithoutSuperCall()
        C().overridden()
        C().overriddenRedundantly()
        C().overriddenBySuperCallOfAnotherMethod()
        C().notOverridden()
    }

}
