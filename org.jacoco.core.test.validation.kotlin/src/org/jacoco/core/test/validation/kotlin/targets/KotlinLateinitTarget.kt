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

import org.junit.Assert.assertEquals

/**
 * Test target for Kotlin lateinit properties
 */
object KotlinLateinitTarget {
    private lateinit var x: String

    private fun testClassProperty() {
        x = "x"
        assertEquals("x", x) // assertFullyCovered()
    }

    private fun testFunctionProperty() {
        lateinit var x: String

        /* This branch is needed otherwise the lateinit branch is optimized out */
        if (1 == 1)
            x = "x"
        assertEquals("x", x) // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        testClassProperty()
        testFunctionProperty()
    }
}

