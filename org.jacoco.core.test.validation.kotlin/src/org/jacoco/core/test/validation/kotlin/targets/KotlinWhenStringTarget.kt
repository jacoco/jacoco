/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
 * Test target with `when` expressions with subject of type `String`.
 */
object KotlinWhenStringTarget {

    private fun whenString(p: String): Int = when (p) { // assertFullyCovered(0, 7)
        "a" -> 1 // assertFullyCovered()
        "b" -> 2 // assertFullyCovered()
        "c" -> 3 // assertFullyCovered()
        "\u0000a" -> 4 // assertFullyCovered()
        "\u0000b" -> 5 // assertFullyCovered()
        "\u0000c" -> 6 // assertFullyCovered()
        else -> 7 // assertFullyCovered()
    } // assertFullyCovered()

    /**
     * Unlike [whenString]
     * in this example first case is the only case with biggest hashCode value.
     * FIXME https://github.com/jacoco/jacoco/issues/1295
     */
    private fun whenStringBiggestHashCodeFirst(p: String): Int = when (p) { // assertPartlyCovered(3, 11)
        "c" -> 1 // assertFullyCovered()
        "b" -> 2 // assertFullyCovered()
        "\u0000b" -> 3 // assertFullyCovered()
        "a" -> 4 // assertFullyCovered()
        "\u0000a" -> 5 // assertFullyCovered()
        else -> 6 // assertFullyCovered()
    } // assertFullyCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        whenString("")
        whenString("a")
        whenString("b")
        whenString("c")
        whenString("\u0000a")
        whenString("\u0000b")
        whenString("\u0000c")

        whenStringBiggestHashCodeFirst("")
        whenStringBiggestHashCodeFirst("a")
        whenStringBiggestHashCodeFirst("b")
        whenStringBiggestHashCodeFirst("c")
        whenStringBiggestHashCodeFirst("\u0000a")
        whenStringBiggestHashCodeFirst("\u0000b")
    }

}
