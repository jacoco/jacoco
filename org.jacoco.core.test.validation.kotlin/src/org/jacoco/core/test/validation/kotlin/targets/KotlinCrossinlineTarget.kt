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
            /* TODO next two lines are partly covered due to regression in Kotlin compiler version 2.0
            https://github.com/jacoco/jacoco/issues/1840
            https://youtrack.jetbrains.com/issue/KT-74617/Trivial-SMAP-optimization-leads-to-missing-debug-info-after-inline
            */
            requireCrossinline { lambda() } // assertPartlyCovered()
        } // assertPartlyCovered()
    } // assertEmpty()

    fun requireCrossinline(lambda: () -> Unit) = lambda()

    @JvmStatic
    fun main(args: Array<String>) {

        example { // assertFullyCovered()
            nop() // assertFullyCovered()
        }() // assertFullyCovered()

    }

}
