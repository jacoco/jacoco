/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
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

    private fun whenSealed(p: Sealed): Int = when (p) { // $line-whenSealed.when$
        is Sealed.Sealed1 -> 1 // $line-whenSealed.case1$
        is Sealed.Sealed2 -> 2 // $line-whenSealed.case2$
    } // $line-whenSealed.return$

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    private fun whenSealedRedundantElse(p: Sealed): Int = when (p) { // $line-whenSealedRedundantElse.when$
        is Sealed.Sealed1 -> 1 // $line-whenSealedRedundantElse.case1$
        is Sealed.Sealed2 -> 2 // $line-whenSealedRedundantElse.case2$
        else -> throw NoWhenBranchMatchedException() // $line-whenSealedRedundantElse.else$
    } // $line-whenSealedRedundantElse.return$

    private enum class Enum {
        A, B
    }

    private fun whenEnum(p: Enum): Int = when (p) { // $line-whenEnum.when$
        Enum.A -> 1 // $line-whenEnum.case1$
        Enum.B -> 2 // $line-whenEnum.case2$
    } // $line-whenEnum.return$

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    private fun whenEnumRedundantElse(p: Enum): Int = when (p) { // $line-whenEnumRedundantElse.when$
        Enum.A -> 1 // $line-whenEnumRedundantElse.case1$
        Enum.B -> 2 // $line-whenEnumRedundantElse.case2$
        else -> throw NoWhenBranchMatchedException() // $line-whenEnumRedundantElse.else$
    } // $line-whenEnumRedundantElse.return$

    private fun whenString(p: String): Int = when (p) { // $line-whenString.when$
        "a" -> 1 // $line-whenString.case1$
        "b" -> 2 // $line-whenString.case2$
        "\u0000a" -> 3 // $line-whenString.case3$
        else -> 4 // $line-whenString.else$
    } // $line-whenString.return$

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
    }

}
