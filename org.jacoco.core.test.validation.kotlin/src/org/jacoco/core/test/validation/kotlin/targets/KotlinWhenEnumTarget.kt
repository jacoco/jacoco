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
 * Test target with `when` expressions with subject of type `enum class`.
 */
object KotlinWhenEnumTarget {

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

    @JvmStatic
    fun main(args: Array<String>) {
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
    }

}
