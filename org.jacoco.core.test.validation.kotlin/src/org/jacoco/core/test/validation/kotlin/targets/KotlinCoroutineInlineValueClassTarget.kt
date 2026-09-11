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

import kotlinx.coroutines.runBlocking
import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target with invocation of suspending function that returns inline value class.
 */
object KotlinCoroutineInlineValueClassTarget {

    private suspend fun suspensionPointReturningInlineValueClass() = InlineValueClass("")

    @JvmInline
    private value class InlineValueClass(val value: String)

    private suspend fun suspendingFunction() { // assertEmpty()
        nop(suspensionPointReturningInlineValueClass()) // assertFullyCovered()
    } // assertFullyCovered()

    private fun suspendingLambda() {
        runBlocking { // assertFullyCovered()
            nop(suspensionPointReturningInlineValueClass()) // assertFullyCovered()
        } // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            suspendingFunction()
            suspendingLambda()
        }
    }

}
