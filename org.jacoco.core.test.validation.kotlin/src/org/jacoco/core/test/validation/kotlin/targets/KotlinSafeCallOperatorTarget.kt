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
 * Test target for [safe call operator (`?.`)](https://kotlinlang.org/docs/null-safety.html#safe-call-operator).
 */
object KotlinSafeCallOperatorTarget {

    data class A(val b: B)
    data class B(val c: String)

    private fun safeCall() {
        fun nullOnly(b: B?): String? =
            b?.c // assertPartlyCovered(1, 1)

        fun nonNullOnly(b: B?): String? =
            b?.c // assertPartlyCovered(1, 1)

        fun fullCoverage(b: B?): String? =
            b?.c // assertFullyCovered(0, 2)

        nullOnly(null)
        nonNullOnly(B(""))
        fullCoverage(null)
        fullCoverage(B(""))
    }

    private fun safeCallChain() {
        fun nullOnly(a: A?): String? =
            a?.b?.c // assertPartlyCovered(2, 2)

        fun nonNullOnly(a: A?): String? =
            a?.b?.c // assertPartlyCovered(2, 2)

        fun fullCoverage(a: A?): String? =
            a?.b?.c // assertFullyCovered(0, 4)

        nullOnly(null)
        nonNullOnly(A(B("")))
        fullCoverage(null)
        fullCoverage(A(B("")))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        safeCall()
        safeCallChain()
    }

}
