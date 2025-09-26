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

/**
 * Test target with invocation of suspending function that returns inline value class.
 */
object KotlinCoroutineInlineValueClassTarget {

    suspend fun suspensionPointReturningInlineValueClass() = InlineValueClass("")

    @JvmInline
    value class InlineValueClass(val value: String)

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking { // assertFullyCovered()
            nop(suspensionPointReturningInlineValueClass()) // assertFullyCovered()
        } // assertFullyCovered()
    }

}
