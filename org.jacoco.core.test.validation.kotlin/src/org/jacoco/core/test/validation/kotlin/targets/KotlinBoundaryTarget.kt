/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

/**
 * Test target for the boundary counter. Every function contains the comparisons
 * of exactly one Kotlin construct, so that the boundary counter of the function
 * is the counter of that construct.
 */
class KotlinBoundaryTarget {

    fun greaterThan(arg: Int): Boolean =
        arg > 6

    fun inRange(arg: Int): Boolean =
        arg in 1..10

    fun whenWithComparison(arg: Int): String =
        when {
            arg > 6 -> "big"
            else -> "small"
        }

    fun whenWithRange(arg: Int): String =
        when (arg) {
            in 1..10 -> "in"
            else -> "out"
        }

    fun elvisThenComparison(arg: Int?): Boolean =
        (arg ?: 0) > 6

    fun nullableComparison(arg: Int?): Boolean =
        arg != null && arg > 6

    fun longGreaterThan(arg: Long): Boolean =
        arg > 6L

    fun comparableOperator(a: String, b: String): Boolean =
        a > b

    inline fun isBig(value: Int): Boolean =
        value > 6

    fun callsInline(arg: Int): Boolean =
        isBig(arg)

}
