/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        is Sealed.Sealed2 -> 2 // assertFullyCovered(1, 1)
        else -> throw NoWhenBranchMatchedException() // assertNotCovered()
    } // assertFullyCovered()

    private enum class Enum {
        A, B
    }

    private fun whenEnum(p: Enum): Int = when (p) {  // assertFullyCovered(0, 2)
        Enum.A -> 1 // assertFullyCovered()
        Enum.B -> 2 // assertFullyCovered()
    } // assertFullyCovered()

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    private fun whenEnumRedundantElse(p: Enum): Int = when (p) { // assertFullyCovered(1, 2)
        Enum.A -> 1 // assertFullyCovered()
        Enum.B -> 2 // assertFullyCovered()
        else -> throw NoWhenBranchMatchedException() // assertNotCovered()
    } // assertFullyCovered()

    private fun whenString(p: String): Int = when (p) { // assertFullyCovered(0, 5)
        "a" -> 1 // assertFullyCovered()
        "b" -> 2 // assertFullyCovered()
        "\u0000a" -> 3 // assertFullyCovered()
        "\u0000b" -> 4 // assertFullyCovered()
        else -> 5 // assertFullyCovered()
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
        whenString("\u0000a")
        whenString("\u0000b")
    }

}
