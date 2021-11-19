/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
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
 * This test target is `lateinit` property.
 */
object KotlinLateinitTarget {
    private lateinit var x: String
    lateinit var y: String

    private class Example<T: Any> {
        private lateinit var x: T
        private lateinit var xx: Any
        lateinit var y: T
        lateinit var yy: Any
        fun nop() {
            x = Any() as T
            xx = Any()
            y = Any() as T
            yy = Any()
            nop(x) // assertFullyCovered()
            nop(xx) // assertFullyCovered()
            nop(y) // assertFullyCovered()
            nop(yy) // assertFullyCovered()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        x = ""
        y = ""

        nop(x) // assertFullyCovered()
        nop(y) // assertFullyCovered()
        Example<Any>().nop() // assertFullyCovered()
    }
}
