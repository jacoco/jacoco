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

/**
 * Test target with `when` expressions and statements whose conditions are
 * ranges.
 *
 * Unlike cases with constants, that are compiled into a single `tableswitch`
 * or `lookupswitch` instruction, every range case is compiled into a
 * comparison of the subject with both bounds, whose result is materialized as
 * a boolean value that is then compared with zero, so that a single case
 * produces six branches:
 *
 * <pre>
 * ICONST_1
 * ILOAD 2
 * IF_ICMPGT out_of_range
 * ILOAD 2
 * BIPUSH 6
 * IF_ICMPGE out_of_range
 * ...
 * IFEQ next_case
 * </pre>
 *
 * @see <a href="https://youtrack.jetbrains.com/issue/KT-19162">KT-19162</a>
 */
object KotlinWhenRangeTarget {

    private fun whenRange(p: Int): Int = when (p) { // assertFullyCovered()
        in 1..5 -> 1 // assertFullyCovered(0, 6)
        in 6..10 -> 2 // assertFullyCovered(0, 6)
        else -> 3 // assertFullyCovered()
    } // assertFullyCovered()

    private fun whenRangeNotIn(p: Int): Int = when (p) { // assertFullyCovered()
        !in 1..10 -> 1 // assertFullyCovered(0, 6)
        else -> 2 // assertFullyCovered()
    } // assertFullyCovered()

    private fun whenRangeStatement(p: Int) { // assertEmpty()
        when (p) { // assertFullyCovered()
            in 1..5 -> nop("1..5") // assertFullyCovered(0, 6)
            in 6..10 -> nop("6..10") // assertFullyCovered(0, 6)
        } // assertEmpty()
    } // assertFullyCovered()

    /**
     * A case with a constant is compiled into a comparison with a single
     * branch, unlike the range case that follows it.
     */
    private fun whenRangeAndConstant(p: Int): Int = when (p) { // assertFullyCovered()
        0 -> 1 // assertFullyCovered(0, 2)
        in 1..5 -> 2 // assertFullyCovered(0, 6)
        else -> 3 // assertFullyCovered()
    } // assertFullyCovered()

    /**
     * Unlike [whenRange] this example has no subject.
     */
    private fun whenWithoutSubject(p: Int): Int = when { // assertFullyCovered()
        p in 1..5 -> 1 // assertFullyCovered(0, 6)
        p in 6..10 -> 2 // assertFullyCovered(0, 6)
        else -> 3 // assertFullyCovered()
    } // assertFullyCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        whenRange(0)
        whenRange(1)
        whenRange(6)
        whenRange(11)

        whenRangeNotIn(0)
        whenRangeNotIn(1)
        whenRangeNotIn(11)

        whenRangeStatement(0)
        whenRangeStatement(1)
        whenRangeStatement(6)
        whenRangeStatement(11)

        whenRangeAndConstant(-1)
        whenRangeAndConstant(0)
        whenRangeAndConstant(1)
        whenRangeAndConstant(6)

        whenWithoutSubject(0)
        whenWithoutSubject(1)
        whenWithoutSubject(6)
        whenWithoutSubject(11)
    }

}
