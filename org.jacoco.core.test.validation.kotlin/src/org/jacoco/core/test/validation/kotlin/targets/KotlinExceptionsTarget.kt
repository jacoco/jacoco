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
 * Test target with exception based control flow examples.
 */
object KotlinExceptionsTarget {

    private fun implicitArrayIndexOutOfBoundsException(a: Array<String>) {
        nop() // assertNotCovered()
        a[0] // assertNotCovered()
        nop() // assertNotCovered()
    }

    private fun implicitException() {
        nop() // assertFullyCovered()
        ex() // assertNotCovered()
        nop() // assertNotCovered()
    }

    private fun explicitException() {
        nop() // assertFullyCovered()
        throw StubException() // assertFullyCovered()
    }

    private fun noExceptionTryCatch() {
        nop() // assertFullyCovered()
        try { // assertFullyCovered()
            nop() // assertFullyCovered()
        } catch (e: StubException) { // assertNotCovered()
            nop() // assertNotCovered()
        } // assertEmpty()
    }

    private fun implicitExceptionTryCatch() {
        nop() // assertFullyCovered()
        try { // assertFullyCovered()
            nop() // assertFullyCovered()
            ex() // assertNotCovered()
            nop() // assertNotCovered()
        } catch (e: StubException) { // assertFullyCovered()
            nop() // assertFullyCovered()
        } // assertEmpty()
    }

    private fun explicitExceptionTryCatch() {
        nop() // assertFullyCovered()
        try { // assertFullyCovered()
            nop() // assertFullyCovered()
            throw StubException() // assertFullyCovered()
        } catch (e: StubException) { // assertFullyCovered()
            nop() // assertFullyCovered()
        } // assertEmpty()
    }

    private fun noExceptionFinally() {
        nop() // assertFullyCovered()
        try { // assertFullyCovered()
            nop() // assertFullyCovered()
        } finally { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertEmpty()
    }

    private fun implicitExceptionFinally() {
        nop() // assertFullyCovered()
        try { // assertFullyCovered()
            nop() // assertFullyCovered()
            ex() // assertNotCovered()
            nop() // assertNotCovered()
        } finally { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertEmpty()
    }

    private fun explicitExceptionFinally() {
        nop() // assertFullyCovered()
        try { // assertFullyCovered()
            nop() // assertFullyCovered()
            throw StubException() // assertFullyCovered()
        } finally { // assertEmpty()
            nop() // assertFullyCovered()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            implicitArrayIndexOutOfBoundsException(arrayOf())
        } catch (_: ArrayIndexOutOfBoundsException) {
        }
        try {
            implicitException()
        } catch (_: StubException) {
        }
        try {
            explicitException()
        } catch (_: StubException) {
        }
        noExceptionTryCatch()
        try {
            implicitExceptionTryCatch()
        } catch (_: StubException) {
        }
        try {
            explicitExceptionTryCatch()
        } catch (_: StubException) {
        }
        noExceptionFinally()
        try {
            implicitExceptionFinally()
        } catch (_: StubException) {
        }
        try {
            explicitExceptionFinally()
        } catch (_: StubException) {
        }
    }

}
