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

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for `inline class`.
 */
object KotlinInlineClassTarget {

    @JvmInline
    value class Example( // assertEmpty()
        val value: String // assertNotCovered()
    ) { // assertEmpty()
        init {
            nop() // assertFullyCovered()
        }

        val property: Int
            get() = value.length // assertFullyCovered()

        fun function() {
            nop() // assertFullyCovered()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val v = Example("")
        v.value
        v.property
        v.function()
    }

}
