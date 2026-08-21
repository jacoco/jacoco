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

import org.jacoco.core.test.validation.targets.Stubs.nop
import org.jacoco.core.test.validation.targets.Stubs.string

/**
 * Test target with `when` expressions and statements with subject of type `String`.
 */
object KotlinWhenStringTarget {

    private fun whenString(p: String): String =
        when (p) { // assertFullyCovered(0, 7)
            "a" -> "case a" // assertFullyCovered()
            "b" -> "case b" // assertFullyCovered()
            "c" -> "case c" // assertFullyCovered()
            "\u0000a" -> "case \u0000a" // assertFullyCovered()
            "\u0000b" -> "case \u0000b" // assertFullyCovered()
            "\u0000c" -> "case \u0000c" // assertFullyCovered()
            else -> "else" // assertFullyCovered()
        } // assertFullyCovered()

    private fun whenStringNullableDefault(p: String?): String =
        when (p) { // assertFullyCovered(0, 4)
            "a" -> "case a" // assertFullyCovered()
            "b" -> "case b" // assertFullyCovered()
            "c" -> "case c" // assertFullyCovered()
            else -> "else" // assertFullyCovered()
        } // assertFullyCovered()

    private fun whenStringNullableCase(p: String?): String =
        when (p) { // assertFullyCovered(0, 5)
            "a" -> "case a" // assertFullyCovered()
            "b" -> "case b" // assertFullyCovered()
            "c" -> "case c" // assertFullyCovered()
            null -> "null" // assertFullyCovered()
            else -> "else" // assertFullyCovered()
        } // assertFullyCovered()

    /**
     * @see KotlinControlStructuresTarget.whenImplicitElseNotExecuted
     */
    private fun implicitElseNotExecuted(s: String) {
        when (s) { // assertFullyCovered(1, 3)
            "a" -> nop("case a") // assertFullyCovered()
            "b" -> nop("case b") // assertFullyCovered()
            "c" -> nop("case c") // assertFullyCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    private fun executedWithSameHashCodeAsFirstCase() {
        when (string("\u0000a")) { // assertFullyCovered(3, 1)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertNotCovered()
            "c" -> nop("case c") // assertNotCovered()
            else -> nop("else") // assertFullyCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    /**
     * Unlike [whenString]
     * in this example first case is the only case with biggest hashCode value.
     */
    private fun whenStringBiggestHashCodeFirst(p: String): String =
        when (p) { // assertFullyCovered(0, 6)
            "c" -> "case c" // assertFullyCovered()
            "b" -> "case b" // assertFullyCovered()
            "\u0000b" -> "case \u0000b" // assertFullyCovered()
            "a" -> "case a" // assertFullyCovered()
            "\u0000a" -> "case \u0000a" // assertFullyCovered()
            else -> "else" // assertFullyCovered()
        } // assertFullyCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        whenString("")
        whenString("a")
        whenString("b")
        whenString("c")
        whenString("\u0000a")
        whenString("\u0000b")
        whenString("\u0000c")

        whenStringNullableDefault("a")
        whenStringNullableDefault("b")
        whenStringNullableDefault("c")
        whenStringNullableDefault("")

        whenStringNullableCase("a")
        whenStringNullableCase("b")
        whenStringNullableCase("c")
        whenStringNullableCase(null)
        whenStringNullableCase("")

        implicitElseNotExecuted("a")
        implicitElseNotExecuted("b")
        implicitElseNotExecuted("c")

        executedWithSameHashCodeAsFirstCase()

        whenStringBiggestHashCodeFirst("")
        whenStringBiggestHashCodeFirst("a")
        whenStringBiggestHashCodeFirst("b")
        whenStringBiggestHashCodeFirst("c")
        whenStringBiggestHashCodeFirst("\u0000a")
        whenStringBiggestHashCodeFirst("\u0000b")
    }

}
