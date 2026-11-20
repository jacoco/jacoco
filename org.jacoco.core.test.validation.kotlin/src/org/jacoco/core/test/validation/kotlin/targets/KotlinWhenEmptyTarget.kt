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
 * Test target with empty `when`.
 */
object KotlinWhenEmptyTarget {

    /**
     * Since Kotlin 2.3.20 subject of empty `when` is evaluated, see
     * [KT-82844](https://youtrack.jetbrains.com/issue/KT-82844).
     */
    private fun emptyWhenWithSideEffectInSubject() {
        var a = 1 // assertFullyCovered()
        when (a++) {} // assertFullyCovered()
        nop(a) // assertFullyCovered()
    }

    private fun emptyWhenWithoutSideEffectInSubject(a: Int) {
        when (a) {} // assertFullyCovered()
        nop(a) // assertFullyCovered()
    }

    private fun emptyWhenWithoutSubject() {
        when {} // assertEmpty()
        nop() // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        emptyWhenWithSideEffectInSubject()
        emptyWhenWithoutSideEffectInSubject(1)
        emptyWhenWithoutSubject()
    }

}
