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

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target containing [JvmOverloads].
 */
object KotlinJvmOverloadsTarget {

    @JvmOverloads // assertFullyCovered()
    fun example(p1: String = "p1", p2: String = "p2") { // assertFullyCovered()
        nop(p1 + p2) // assertFullyCovered()
    } // assertFullyCovered()

    private fun use() { // assertEmpty()
        example() // assertFullyCovered()
    } // assertFullyCovered()

    private fun useOneliner() = example() // assertFullyCovered()

    class Example @JvmOverloads constructor(p1: String = "p1", p2: String = "p2") { // assertFullyCovered()
        constructor(p: Any) : this() // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        example()
        example("")
        example("", "")

        use()
        useOneliner()

        Example()
        Example("")
        Example("", "")

        Example(Any())
    }

}
