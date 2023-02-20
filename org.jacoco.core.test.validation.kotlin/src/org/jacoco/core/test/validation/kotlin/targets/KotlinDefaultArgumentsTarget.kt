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

/**
 * Test target for functions with default arguments.
 */
object KotlinDefaultArgumentsTarget {

    private fun f(a: String = "a", b: String = "b") { // assertFullyCovered(0, 0)
    }

    private fun longParameter(x: Long = 0) { // assertFullyCovered()
    }

    private fun branch(a: Boolean, b: String = if (a) "a" else "b") { // assertFullyCovered(0, 2)
    }

    open class Open {
        open fun f(a: String = "a") { // assertFullyCovered()
        }
    }

    class Constructor() {
        constructor(a: Boolean, b: String = if (a) "a" else "b") : this() // assertFullyCovered(0, 2)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        f(a = "a")
        f(b = "b")
        /* next invocation doesn't use synthetic method: */
        f("a", "b")

        longParameter()
        longParameter(1)

        branch(false)
        branch(true)

        Open().f()

        Constructor(false)
        Constructor(true)
    }

}
