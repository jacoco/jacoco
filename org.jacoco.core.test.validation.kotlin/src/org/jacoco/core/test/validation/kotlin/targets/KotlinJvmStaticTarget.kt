/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
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
 * Test target containing [JvmStatic] functions.
 */
object KotlinJvmStaticTarget {

    interface Interface {
        companion object {
            @JvmStatic // assertEmpty()
            fun target() { // assertEmpty()
                nop() // assertFullyCovered()
            } // assertFullyCovered()
        }
    }

    class Class {
        companion object {
            @JvmStatic // assertEmpty()
            fun target() { // assertEmpty()
                nop() // assertFullyCovered()
            } // assertFullyCovered()
        }
    }

    object NamedObject {
        @JvmStatic // assertEmpty()
        fun target() { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Interface.target()
        Class.target()
        NamedObject.target()
    }

}
