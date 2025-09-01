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

import kotlinx.coroutines.runBlocking

/**
 * Test target with suspending function and lambda with calls to suspending function returning inline value class.
 */
object KotlinCoroutineInlineValueClassTarget {

    suspend fun suspendingFunction() { // assertEmpty()
        afterSuspensionPoint( // assertFullyCovered()
            beforeSuspensionPoint(), // assertFullyCovered()
            suspendingFunctionReturningInlineValueClass() // assertFullyCovered()
        ) // assertEmpty()
    } // assertFullyCovered()

    fun suspendingLambda() {
        runBlocking { // assertFullyCovered()
            afterSuspensionPoint( // assertFullyCovered()
                beforeSuspensionPoint(), // assertFullyCovered()
                suspendingFunctionReturningInlineValueClass() // assertFullyCovered()
            ) // assertEmpty()
        } // assertFullyCovered()
    }

    suspend fun suspendingFunctionReturningInlineValueClass() = InlineValueClass("")

    @JvmInline
    value class InlineValueClass(val value: String)

    fun afterSuspensionPoint(a: Any, b: Any) = Unit

    fun beforeSuspensionPoint() = Any()

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            suspendingFunction()
            suspendingLambda()
        }
    }

}
