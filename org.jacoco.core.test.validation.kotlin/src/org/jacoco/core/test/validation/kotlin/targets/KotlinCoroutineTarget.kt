/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        } // assertFullyCovered()

    }

}
