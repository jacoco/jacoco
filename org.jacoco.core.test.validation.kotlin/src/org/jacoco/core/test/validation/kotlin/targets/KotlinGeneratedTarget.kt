/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for Kotlin generated methods
 */
object KotlinGeneratedTarget {
    private fun testTrivial() {
        data class Target(var value: Int)  // assertFullyCovered()

        val target = Target(0)
        target.value = 1
        nop(target.value)
    }

    /**
     * Immutable properties should have the getter tested.
     */
    private fun testImmutablePropertyNotAccessed() {
        data class Target(val value: Int)  // assertPartlyCovered()
        Target(0)
    }

    /**
     * Mutable properties should have both their getters and setters tested.
     */
    private fun testMutablePropertyNotChanged() {
        data class Target(var value: Int)  // assertPartlyCovered()

        val target = Target(0)
        nop(target.value)
    }

    /**
     * Methods in data classes that are usually generated, but are overridden
     * by the user should not be covered by default.
     */
    private fun testOverrides() {
        data class Target(val value: Int) { // assertFullyCovered()
            override fun toString(): String = "" // assertNotCovered()
        }

        val target = Target(0)
        nop(target.value)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        testTrivial()
        testImmutablePropertyNotAccessed()
        testMutablePropertyNotChanged()
        testOverrides()
    }
}
