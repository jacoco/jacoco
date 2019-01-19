/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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
 * This test target is `lateinit` property.
 */
object KotlinLateinitTarget {
    private lateinit var x: String

    @JvmStatic
    fun main(args: Array<String>) {
        x = ""
        nop(x) // assertFullyCovered()
    }
}
