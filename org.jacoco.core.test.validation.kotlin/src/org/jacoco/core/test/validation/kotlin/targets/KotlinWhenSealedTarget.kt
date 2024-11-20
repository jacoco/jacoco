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
 * Test target with `when` expressions with subject of type `enum class`.
 */
object KotlinWhenSealedTarget {

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

    @JvmStatic
    fun main(args: Array<String>) {
        whenSealed(Sealed.Sealed1)
        whenSealed(Sealed.Sealed2)

        whenSealedRedundantElse(Sealed.Sealed1)
        whenSealedRedundantElse(Sealed.Sealed2)
    }

}
