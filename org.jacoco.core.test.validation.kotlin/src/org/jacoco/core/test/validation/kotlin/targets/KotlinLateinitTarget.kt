/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for Kotlin lateinit properties
 */
object KotlinLateinitTarget {
    private lateinit var x: String

    private fun testClassProperty() {
        x = ""
        nop(x) // assertFullyCovered()
    }

    private fun testFunctionProperty() {
        lateinit var x: String

        /* This branch is needed as to not optimize away the assignment */
        if (1 == 1)
            x = ""

        nop(x) // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        testClassProperty()
        testFunctionProperty()
    }
}

