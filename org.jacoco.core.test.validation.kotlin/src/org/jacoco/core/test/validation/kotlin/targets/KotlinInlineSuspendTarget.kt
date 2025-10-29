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

import kotlinx.coroutines.runBlocking

/**
 * Test target containing `suspend` `inline` function.
 */
object KotlinInlineSuspendTarget {

    suspend fun suspendFun(): String = ""

    suspend inline fun suspendInlineFun(f: () -> String): String { // assertEmpty()
        return suspendFun() + f() // assertFullyCovered()
    } // assertEmpty()

    suspend inline fun suspendInlineFunNotExecuted(f: () -> String): String { // assertEmpty()
        return suspendFun() + f() // assertNotCovered()
    } // assertEmpty()

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            suspendInlineFun { "" }
        }
    }

}
