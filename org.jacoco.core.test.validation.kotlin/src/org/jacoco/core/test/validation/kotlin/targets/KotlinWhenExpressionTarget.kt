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
 * This test target is `when` expression.
 */
object KotlinWhenExpressionTarget {

    private sealed class Sealed {
        object Sealed1 : Sealed()
        object Sealed2 : Sealed()
    }

    private fun whenSealed(p: Sealed): Int = when (p) { // assertFullyCovered()
        is Sealed.Sealed1 -> 1 // assertFullyCovered(0, 2)
        is Sealed.Sealed2 -> 2 // assertFullyCovered()
    } // assertFullyCovered()

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    private fun whenSealedRedundantElse(p: Sealed): Int = when (p) { // assertFullyCovered()
        is Sealed.Sealed1 -> 1 // assertFullyCovered(0, 2)
        is Sealed.Sealed2 -> 2 // assertFullyCovered(0, 0)
        else -> throw NoWhenBranchMatchedException() // assertEmpty()
    } // assertFullyCovered()

    private enum class Enum {
        A, B
    }

    private fun whenEnum(p: Enum): Int = when (p) {  // assertFullyCovered(0, 2)
        Enum.A -> 1 // assertFullyCovered()
        Enum.B -> 2 // assertFullyCovered()
    } // assertFullyCovered()

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    private fun whenEnumRedundantElse(p: Enum): Int = when (p) { // assertFullyCovered(0, 2)
        Enum.A -> 1 // assertFullyCovered()
        Enum.B -> 2 // assertFullyCovered()
        else -> throw NoWhenBranchMatchedException() // assertEmpty()
    } // assertFullyCovered()

    private fun whenString(p: String): Int = when (p) { // assertFullyCovered(0, 7)
        "a" -> 1 // assertFullyCovered()
        "b" -> 2 // assertFullyCovered()
        "c" -> 3 // assertFullyCovered()
        "\u0000a" -> 4 // assertFullyCovered()
        "\u0000b" -> 5 // assertFullyCovered()
        "\u0000c" -> 6 // assertFullyCovered()
        else -> 7 // assertFullyCovered()
    } // assertFullyCovered()

    /**
     * Unlike [whenString]
     * in this example first case is the only case with biggest hashCode value.
     */
    private fun whenStringBiggestHashCodeFirst(p: String): Int = when (p) { // assertFullyCovered(0, 6)
        "c" -> 1 // assertFullyCovered()
        "b" -> 2 // assertFullyCovered()
        "\u0000b" -> 3 // assertFullyCovered()
        "a" -> 4 // assertFullyCovered()
        "\u0000a" -> 5 // assertFullyCovered()
        else -> 6 // assertFullyCovered()
    } // assertFullyCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        whenSealed(Sealed.Sealed1)
        whenSealed(Sealed.Sealed2)

        whenSealedRedundantElse(Sealed.Sealed1)
        whenSealedRedundantElse(Sealed.Sealed2)

        whenEnum(Enum.A)
        whenEnum(Enum.B)

        whenEnumRedundantElse(Enum.A)
        whenEnumRedundantElse(Enum.B)

        whenString("")
        whenString("a")
        whenString("b")
        whenString("c")
        whenString("\u0000a")
        whenString("\u0000b")
        whenString("\u0000c")

        whenStringBiggestHashCodeFirst("")
        whenStringBiggestHashCodeFirst("a")
        whenStringBiggestHashCodeFirst("b")
        whenStringBiggestHashCodeFirst("c")
        whenStringBiggestHashCodeFirst("\u0000a")
        whenStringBiggestHashCodeFirst("\u0000b")
    }

}
