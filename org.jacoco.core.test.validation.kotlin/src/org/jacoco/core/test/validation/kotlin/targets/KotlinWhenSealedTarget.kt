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
 * Test target with `when` expressions and statements with subject of type `sealed class`.
 */
object KotlinWhenSealedTarget {

    private sealed class Sealed {
        object Sealed1 : Sealed()
        object Sealed2 : Sealed()
    }

    private fun expression(sealed: Sealed): String =
        when (sealed) { // assertFullyCovered()
            is Sealed.Sealed1 -> "case 1" // assertFullyCovered(0, 2)
            is Sealed.Sealed2 -> "case 2" // assertFullyCovered()
        } // assertFullyCovered()

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    private fun expressionWithRedundantElse(sealed: Sealed): String =
        when (sealed) { // assertFullyCovered()
            is Sealed.Sealed1 -> "case 1" // assertFullyCovered(0, 2)
            is Sealed.Sealed2 -> "case 2" // assertFullyCovered(0, 0)
            else -> throw NoWhenBranchMatchedException() // assertEmpty()
        } // assertFullyCovered()

    /**
     * Since Kotlin 1.7 `when` statement with subject of type `sealed class`
     * must be exhaustive (error otherwise, warning in 1.6), however
     * Kotlin compiler prior to version 2.0 was generating bytecode
     * indistinguishable from [nonSealedWhen] and [nonSealedIf]
     * that do not have coverage for the case of [NonSealed.NonSealed3].
     */
    private fun statement(sealed: Sealed) { // assertEmpty()
        when (sealed) { // assertFullyCovered()
            is Sealed.Sealed1 -> nop("case 1") // assertFullyCovered(0, 2)
            is Sealed.Sealed2 -> nop("case 2") // assertFullyCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    private abstract class NonSealed {
        class NonSealed1 : NonSealed()
        class NonSealed2 : NonSealed()
        class NonSealed3 : NonSealed()
    }

    private fun nonSealedWhen(nonSealed: NonSealed) { // assertEmpty()
        when (nonSealed) { // assertFullyCovered()
            is NonSealed.NonSealed1 -> nop("case 1") // assertFullyCovered(0, 2)
            is NonSealed.NonSealed2 -> nop("case 2") // assertFullyCovered(1, 1)
            /* missing is NonSealed.NonSealed3 */
        } // assertEmpty()
    } // assertFullyCovered()

    private fun nonSealedIf(nonSealed: NonSealed) { // assertEmpty()
        if (nonSealed is NonSealed.NonSealed1) nop("case 1") // assertFullyCovered(0, 2)
        else if (nonSealed is NonSealed.NonSealed2) nop("case 2") // assertFullyCovered(1, 1)
        /* missing is NonSealed.NonSealed3 */
    } // assertFullyCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        expression(Sealed.Sealed1)
        expression(Sealed.Sealed2)

        expressionWithRedundantElse(Sealed.Sealed1)
        expressionWithRedundantElse(Sealed.Sealed2)

        statement(Sealed.Sealed1)
        statement(Sealed.Sealed2)

        nonSealedWhen(NonSealed.NonSealed1())
        nonSealedWhen(NonSealed.NonSealed2())

        nonSealedIf(NonSealed.NonSealed1())
        nonSealedIf(NonSealed.NonSealed2())
    }

}
