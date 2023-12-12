/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lukas Rössler - initial implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

import kotlinx.coroutines.runBlocking
import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * This test targets suspending lambdas.
 */
object KotlinSuspendingLambdaTarget {

    private fun callLambda(suspendingLambda: suspend () -> Unit) = runBlocking {
        suspendingLambda()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        callLambda { // assertFullyCovered()
            nop() // assertFullyCovered()
        } // assertFullyCovered()
    }
}
