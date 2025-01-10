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

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for `inline class`.
 */
object KotlinInlineClassTarget {

    interface Base {
        fun base()
    }

    @JvmInline
    value class I1(val value: String) : Base { // assertEmpty()

        init { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertEmpty()

        constructor() : this("") { // assertFullyCovered()
            nop() // assertFullyCovered()
        } // assertEmpty()

        val length: Int // assertEmpty()
            get() = value.length // assertFullyCovered()

        fun f(p: String) { // assertEmpty()
            nop(p) // assertFullyCovered()
        } // assertFullyCovered()

        fun f(p: I1) { // assertEmpty()
            nop(p) // assertFullyCovered()
        } // assertFullyCovered()

        override fun base() { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertFullyCovered()

    } // assertEmpty()

    @JvmInline
    value class I2(val value: String) { // assertEmpty()

        override fun toString(): String { // assertEmpty()
            return "Value: $value" // assertNotCovered()
        } // assertEmpty()

    } // assertEmpty()

    @JvmStatic
    fun main(args: Array<String>) {
        val i = I1()
        i.value
        i.length
        i.f("")
        i.f(i)
        i.base()

        I2("")
    }

}
