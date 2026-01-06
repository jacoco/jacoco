/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

/**
 * Test target with [guard conditions in `when`](https://kotlinlang.org/docs/control-flow.html#guard-conditions-in-when-expressions).
 */
object KotlinWhenGuardTarget {

    private sealed interface S {
        data class S1(val s: String) : S
        object S2 : S
    }

    private fun example(s: S) { // assertEmpty()
        when (s) { // assertFullyCovered()
            is S.S1 if s.s == "" -> nop() // assertFullyCovered(0, 4)
            is S.S1 -> nop() // assertFullyCovered(0, 2)
            is S.S2 -> nop() // assertFullyCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        example(S.S1(""))
        example(S.S1("s"))
        example(S.S2)
    }

}
