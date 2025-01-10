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
 * Test target for [elvis operator (`?:`)](https://kotlinlang.org/docs/null-safety.html#elvis-operator).
 */
object KotlinElvisOperatorTarget {

    private fun elvis() {
        fun nullOnly(a: String?): String =
            a ?: "" // assertFullyCovered(1, 1)

        fun nonNullOnly(a: String?): String =
            a ?: "" // assertPartlyCovered(1, 1)

        fun fullCoverage(a: String?): String =
            a ?: "" // assertFullyCovered(0, 2)

        nullOnly(null)
        nonNullOnly("")
        fullCoverage(null)
        fullCoverage("")
    }

    private fun elvisChain() {
        fun bothNull(a: String?, b: String?): String =
            a ?: b ?: "" // assertFullyCovered(2, 2)

        fun secondNonNull(a: String?, b: String?): String =
            a ?: b ?: "" // assertPartlyCovered(2, 2)

        fun firstNonNull(a: String?, b: String?): String =
            a ?: b ?: "" // assertPartlyCovered(3, 1)

        fun firstOrSecondNonNull(a: String?, b: String?): String =
            a ?: b ?: "" // assertPartlyCovered(1, 3)

        fun fullCoverage(a: String?, b: String?): String =
            a ?: b ?: "" // assertFullyCovered(0, 4)

        bothNull(null, null)
        secondNonNull(null, "")
        firstNonNull("", null)
        firstNonNull("", "")
        firstOrSecondNonNull("", null)
        firstOrSecondNonNull(null, "")
        fullCoverage(null, null)
        fullCoverage("", null)
        fullCoverage(null, "")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        elvis()
        elvisChain()
    }

}
