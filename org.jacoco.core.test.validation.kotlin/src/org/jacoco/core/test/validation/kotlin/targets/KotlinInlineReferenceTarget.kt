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
import org.jacoco.core.test.validation.targets.Stubs.t

/**
 * Test target with `inline` function invoked via callable reference.
 */
object KotlinInlineReferenceTarget {

    private inline fun example() { // assertEmpty()
        nop() // assertFullyCovered()
    } // assertFullyCovered()

    private fun run(f: () -> Unit) {
        f()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        run(::example)
    }

}
