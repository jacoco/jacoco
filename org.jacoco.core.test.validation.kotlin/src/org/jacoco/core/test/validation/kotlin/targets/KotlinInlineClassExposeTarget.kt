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
 * Test target with [JvmExposeBoxed].
 */
@OptIn(ExperimentalStdlibApi::class)
object KotlinInlineClassExposeTarget {

    @JvmExposeBoxed // assertEmpty()
    @JvmInline // assertEmpty()
    private value class ValueClass(val value: String) // assertEmpty()

    @JvmExposeBoxed // assertEmpty()
    private fun exposeReturnType(): ValueClass = // assertEmpty()
        ValueClass("")

    @JvmExposeBoxed // assertEmpty()
    private fun exposeParameter(p: ValueClass) = // assertEmpty()
        nop(p) // assertFullyCovered()

    @JvmExposeBoxed // assertEmpty()
    private fun exposeUseless() = // assertEmpty()
        nop() // assertFullyCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        exposeReturnType()
        exposeParameter(ValueClass(""))
        exposeUseless()
    }

}
