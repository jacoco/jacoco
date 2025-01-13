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
 * Test target for `crossinline`.
 */
object KotlinCrossinlineTarget {

    inline fun example(crossinline lambda: () -> Unit): () -> Unit { // assertEmpty()
        return { // assertFullyCovered()
            requireCrossinline { lambda() } // assertFullyCovered()
        } // assertFullyCovered()
    } // assertEmpty()

    fun requireCrossinline(lambda: () -> Unit) = lambda()

    @JvmStatic
    fun main(args: Array<String>) {

        example { // assertFullyCovered()
            nop() // assertFullyCovered()
        }() // assertFullyCovered()

    }

}
