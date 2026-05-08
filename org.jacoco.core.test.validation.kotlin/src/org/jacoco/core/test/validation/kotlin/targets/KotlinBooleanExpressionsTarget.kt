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

import org.jacoco.core.test.validation.targets.Stubs.*

/**
 * Test target with [`Boolean` operations](https://kotlinlang.org/docs/booleans.html#boolean-operations).
 */
object KotlinBooleanExpressionsTarget {

    @JvmStatic
    fun main(args: Array<String>) {
        /* Boolean comparison result (one case) */
        nop(i2() > 3) // assertPartlyCovered(1, 1)

        /* Boolean comparison result (both cases) */
        for (i in 0..1) {
            nop(i < 1) // assertFullyCovered(0, 2)
        }

        /* And */
        if (f() and f()) { // assertFullyCovered(1, 1)
            nop()
        }
        if (f() and t()) { // assertFullyCovered(1, 1)
            nop()
        }
        if (t() and f()) { // assertFullyCovered(1, 1)
            nop()
        }
        if (t() and t()) { // assertFullyCovered(1, 1)
            nop()
        }
        nop(f() and f()) // assertFullyCovered()

        /* Conditional And */
        if (f() && t()) { // assertPartlyCovered(3, 1)
            nop()
        }
        if (t() && f()) { // assertFullyCovered(2, 2)
            nop()
        }
        if (t() && t()) { // assertFullyCovered(2, 2)
            nop()
        }
        nop(f() && f()) // assertPartlyCovered(3, 1)
        nop(t() && f()) // assertPartlyCovered(2, 2)

        /* Or */
        if (f() or f()) { // assertFullyCovered(1, 1)
            nop()
        }
        if (f() or t()) { // assertFullyCovered(1, 1)
            nop()
        }
        if (t() or f()) { // assertFullyCovered(1, 1)
            nop()
        }
        if (t() or t()) { // assertFullyCovered(1, 1)
            nop()
        }
        nop(f() or f()) // assertFullyCovered()

        /* Conditional Or */
        if (f() || f()) { // assertFullyCovered(2, 2)
            nop()
        }
        if (f() || t()) { // assertFullyCovered(2, 2)
            nop()
        }
        if (t() || f()) { // assertPartlyCovered(3, 1)
            nop()
        }
        if (t() || t()) { // assertPartlyCovered(3, 1)
            nop()
        }
        nop(t() || f()) // assertPartlyCovered(3, 1)
        nop(f() || f()) // assertPartlyCovered(2, 2)

        /* Exclusive Or */
        if (f() xor f()) { // assertFullyCovered(1, 1)
            nop()
        }
        if (f() xor t()) { // assertFullyCovered(1, 1)
            nop()
        }
        if (t() xor f()) { // assertFullyCovered(1, 1)
            nop()
        }
        if (t() xor t()) { // assertFullyCovered(1, 1)
            nop()
        }
        nop(f() xor f()) // assertFullyCovered()

        /* `if` as expression */
        nop(if (t()) i1() else i2()) // assertPartlyCovered(1, 1)
        nop(if (f()) i1() else i2()) // assertPartlyCovered(1, 1)

        /* Not (one case) */
        nop(!t()) // assertPartlyCovered(1, 1)
        nop(!f()) // assertPartlyCovered(1, 1)

        /* Not (both cases) */
        for (b in booleanArrayOf(true, false)) {
            nop(!b) // assertFullyCovered(0, 2)
        }
    }

}
