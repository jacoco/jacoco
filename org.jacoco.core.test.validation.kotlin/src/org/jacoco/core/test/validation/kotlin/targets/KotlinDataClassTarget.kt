/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
            val valNoRead: Int, // assertNotCovered()
            val valRead: Int,  // assertFullyCovered()
            var varNoReadNoWrite: Int, // assertNotCovered()
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
