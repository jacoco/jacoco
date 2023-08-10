/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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

/**
 * Test target for coroutines.
 */
object KotlinCoroutineTarget {

    private suspend fun suspendingFunction() { // assertEmpty()
        anotherSuspendingFunction() // assertFullyCovered()
        nop() // assertFullyCovered()
    } // assertFullyCovered()

    private suspend fun suspendingFunctionWithTailCallOptimization() { // assertEmpty()
        nop() // assertFullyCovered()
        anotherSuspendingFunction() // assertFullyCovered()
    } // assertFullyCovered()

    private suspend fun anotherSuspendingFunction() {
        nop() // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {

        runBlocking { // assertFullyCovered()
            val x = 42
            nop(x) // assertFullyCovered()
            suspendingFunction() // assertFullyCovered()
            nop(x) // assertFullyCovered()
            suspendingFunctionWithTailCallOptimization()
        } // assertFullyCovered()

    }

}
