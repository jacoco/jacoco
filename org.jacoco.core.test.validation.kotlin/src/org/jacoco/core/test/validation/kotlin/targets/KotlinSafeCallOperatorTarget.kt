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
 * Test target for safe call operator.
 */
object KotlinSafeCallOperatorTarget {

    private fun example(x: String?): Int? {
        return x?.length // assertFullyCovered(0, 2)
    }

    private fun safeCallFollowedByElvis() {
        fun nullOnly(b: B?): String =
            b?.c ?: "" // assertPartlyCovered(2, 2)

        fun nonNullOnly(b: B?): String =
            b?.c ?: "" // assertPartlyCovered(1, 3)

        fun both(b: B?): String =
            b?.c ?: "" // assertFullyCovered(0, 4)

        nullOnly(null)
        nonNullOnly(B(""))
        both(null)
        both(B(""))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        example("")
        example(null)
        safeCallFollowedByElvis()
    }

}
