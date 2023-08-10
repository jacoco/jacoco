/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * This test target is `data class`.
 */
object KotlinDataClassTarget {

    data class DataClass( // assertFullyCovered()
            val valNoRead: Int, // assertPartlyCovered()
            val valRead: Int,  // assertFullyCovered()
            var varNoReadNoWrite: Int, // assertPartlyCovered()
            var varNoWrite: Int, // assertPartlyCovered()
            var varNoRead: Int, // assertPartlyCovered()
            var varReadWrite: Int  // assertFullyCovered()
    ) // assertEmpty()

    data class DataClassOverrideNotCovered(val v: Int) {
        override fun toString(): String = "" // assertNotCovered()
    }

    data class DataClassOverrideCovered(val v: Int) {
        override fun toString(): String = "" // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val d = DataClass(0, 0, 0, 0, 0, 0)
        nop(d.valRead)
        nop(d.varNoWrite)
        d.varNoRead = 1
        nop(d.varReadWrite)
        d.varReadWrite = 1

        DataClassOverrideNotCovered(0)
        DataClassOverrideCovered(0).toString()
    }
}
