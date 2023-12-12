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
import org.jacoco.core.test.validation.targets.Stubs.t

/**
 * Test target for `inline` functions.
 */
fun main(args: Array<String>) {
    KotlinInlineTarget.main(args)
}

inline fun inlined_top_level() {
    nop() // assertNotCovered()
}

object KotlinInlineTarget {

    inline fun inlined() {
        nop() // assertNotCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {

        inlined_top_level() // assertFullyCovered()

        inlined() // assertFullyCovered()

        /* Following inlined method for some reasons doesn't appear in SMAP: */
        assert(t()) // assertPartlyCovered(2, 2)

    }

}
