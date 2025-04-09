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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Test target with suspending function with tail call optimization.
 */
object KotlinCoroutineTailCallOptimizationTarget {

    suspend fun unitReturning(b: Boolean) { // assertEmpty()
        if (b) // assertFullyCovered(0, 2)
            delay() // assertFullyCovered()
        else // assertEmpty()
            noDelay() // assertFullyCovered()
    } // assertEmpty()

    suspend fun delay() = delay(1)
    suspend fun noDelay() = delay(0)

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            unitReturning(true)
            unitReturning(false)
        }
    }

}
