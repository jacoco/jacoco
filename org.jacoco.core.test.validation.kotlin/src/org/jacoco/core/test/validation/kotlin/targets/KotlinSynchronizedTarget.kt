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

import org.jacoco.core.test.validation.targets.Stubs.*

/**
 * Test target with [synchronized] block.
 */
object KotlinSynchronizedTarget {

    private val lock = Any();

    private fun normal() {
        synchronized(lock) { // assertFullyCovered()
            nop() // assertFullyCovered()
        } // assertFullyCovered()
    }

    private fun explicitException() {
        synchronized(lock) { // assertFullyCovered()
            throw StubException() // assertFullyCovered()
        } // assertEmpty()
    }

    private fun implicitException() {
        synchronized(lock) { // assertPartlyCovered()
            ex() // assertNotCovered()
        } // assertNotCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        normal()

        try {
            explicitException()
        } catch (_: StubException) {
        }

        try {
            implicitException()
        } catch (_: StubException) {
        }
    }

}
