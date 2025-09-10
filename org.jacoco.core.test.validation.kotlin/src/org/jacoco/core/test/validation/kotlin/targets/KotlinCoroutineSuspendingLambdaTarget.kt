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
 * Test target containing suspending lambdas.
 */
object KotlinCoroutineSuspendingLambdaTarget {

    private fun withParameter() {
        fun exec(block: suspend (p: String) -> Unit): Unit = runBlocking { block("") }
        fun noexec(block: suspend (p: String) -> Unit) = Unit
        suspend fun suspensionPoint(p: String) = Unit

        exec { p -> // assertFullyCovered()
            suspensionPoint(p) // assertFullyCovered()
        } // assertFullyCovered()

        noexec { p -> // assertFullyCovered()
            suspensionPoint(p) // assertNotCovered()
        } // assertNotCovered()

        noexec { p -> suspensionPoint(p) } // assertPartlyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        withParameter()
    }

}
