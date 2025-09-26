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
import org.jacoco.core.test.validation.targets.Stubs.nop
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Test target containing invocations of [kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn] intrinsic
 * inlined from [suspendCoroutine] which provides a way to
 * [wrap any callback into a Kotlin suspending function](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0164-coroutines.md#wrapping-callbacks).
 *
 * @see [kotlinx.coroutines.suspendCancellableCoroutine]
 */
object KotlinCoroutineCallbackTarget {

    suspend fun example() =
        suspendCoroutine { continuation -> // assertFullyCovered()
            continuation.resume(Unit) // assertFullyCovered()
        } // assertFullyCovered()

    suspend fun withoutTailCallOptimization() {
        suspendCoroutine { continuation -> // assertFullyCovered()
            continuation.resume(Unit) // assertFullyCovered()
        } // assertFullyCovered()
        nop("tail") // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            example()
            withoutTailCallOptimization()
        }
    }

}
