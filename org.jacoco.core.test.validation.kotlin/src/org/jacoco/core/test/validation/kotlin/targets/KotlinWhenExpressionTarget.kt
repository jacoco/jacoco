/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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

    private fun whenByNullableEnumWithNullCaseAndWithoutElse(e: Enum?): String =
        when (e) { // assertFullyCovered(0, 3)
            Enum.A -> "a" // assertFullyCovered()
            Enum.B -> "b" // assertFullyCovered()
            null -> "null" // assertFullyCovered()
        } // assertFullyCovered()

    private fun whenByNullableEnumWithoutNullCaseAndWithElse(e: Enum?): String =
        when (e) { // assertFullyCovered(0, 3)
            Enum.A -> "a" // assertFullyCovered()
            Enum.B -> "b" // assertFullyCovered()
            else -> "else" // assertFullyCovered()
        } // assertFullyCovered()

    private fun whenByNullableEnumWithNullAndElseCases(e: Enum?): String =
        when (e) { // assertFullyCovered(0, 3)
            Enum.A -> "a" // assertFullyCovered()
            null -> "null" // assertFullyCovered()
            else -> "else" // assertFullyCovered()
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
     * FIXME https://github.com/jacoco/jacoco/issues/1295
     */
    private fun whenStringBiggestHashCodeFirst(p: String): Int = when (p) { // assertPartlyCovered(3, 11)
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

        whenByNullableEnumWithNullCaseAndWithoutElse(Enum.A)
        whenByNullableEnumWithNullCaseAndWithoutElse(Enum.B)
        whenByNullableEnumWithNullCaseAndWithoutElse(null)

        whenByNullableEnumWithoutNullCaseAndWithElse(Enum.A)
        whenByNullableEnumWithoutNullCaseAndWithElse(Enum.B)
        whenByNullableEnumWithoutNullCaseAndWithElse(null)

        whenByNullableEnumWithNullAndElseCases(Enum.A)
        whenByNullableEnumWithNullAndElseCases(Enum.B)
        whenByNullableEnumWithNullAndElseCases(null)

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
