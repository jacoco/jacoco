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

import org.jacoco.core.test.validation.targets.Stubs.*

/**
 * Test target for Kotlin control structures.
 */
object KotlinControlStructuresTarget {

    private fun unconditionalExecution() {

        nop() // assertFullyCovered()

    }

    private fun missedIfBlock() {

        if (f()) { // assertFullyCovered(1, 1)
            nop() // assertNotCovered()
        } else {
            nop() // assertFullyCovered()
        }

    }

    private fun executedIfBlock() {

        if (t()) { // assertFullyCovered(1, 1)
            nop() // assertFullyCovered()
        } else {
            nop() // assertNotCovered()
        }

    }

    private fun missedWhileBlock() {

        while (f()) { // assertFullyCovered(1, 1)
            nop() // assertNotCovered()
        }

    }

    private fun executedWhileBlock() {

        var i = 0
        while (i++ < 3) { // assertFullyCovered(0, 2)
            nop() // assertFullyCovered()
        }

    }

    private fun executedDoWhileBlock() {

        do {
            nop() // assertFullyCovered()
        } while (f()) // assertFullyCovered(1, 1)

    }

    private fun missedForBlock() {

        for (j in i2()..i1()) { // assertPartlyCovered(3, 1)
            nop() // assertNotCovered()
        }

    }

    private fun executedForBlock() {

        for (j in i1()..i2()) { // assertFullyCovered(1, 3)
            nop() // assertFullyCovered()
        }

    }

    private fun missedForEachBlock() {

        for (o in emptyList<Any>()) { // assertPartlyCovered(1, 1)
            nop(o) // assertNotCovered()
        }

    }

    private fun executedForEachBlock() {

        for (o in setOf(Any())) { // assertFullyCovered(0,2)
            nop(o) // assertFullyCovered()
        }

    }

    private fun whenExpression() {

        when (i2()) { // assertFullyCovered(2, 1)
            1 -> nop() // assertNotCovered()
            2 -> nop() // assertFullyCovered()
            else -> nop() // assertNotCovered()
        }

    }

    private fun breakStatement() {

        while (true) {
            if (t()) {
                break // assertFullyCovered()
            }
            nop() // assertNotCovered()
        }

    }

    private fun continueStatement() {

        for (j in i1()..i2()) {
            if (t()) {
                continue // assertFullyCovered()
            }
            nop() // assertNotCovered()
        }

    }

    private fun implicitReturn() {
    } // assertFullyCovered()

    private fun explicitReturn() {

        return // assertFullyCovered()

    } // assertEmpty()

    @JvmStatic
    fun main(args: Array<String>) {
        unconditionalExecution()
        missedIfBlock()
        executedIfBlock()
        missedWhileBlock()
        executedWhileBlock()
        executedDoWhileBlock()
        missedForBlock()
        executedForBlock()
        missedForEachBlock()
        executedForEachBlock()
        whenExpression()
        breakStatement()
        continueStatement()
        implicitReturn()
        explicitReturn()
    }

}
