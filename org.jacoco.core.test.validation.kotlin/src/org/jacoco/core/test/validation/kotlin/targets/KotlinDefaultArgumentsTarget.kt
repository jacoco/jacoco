/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

/**
 * This test target is function with default arguments.
 */
object KotlinDefaultArgumentsTarget {

    private fun f(a: String = "a", b: String = "b") { // assertFullyCovered(0, 2)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        f(a = "a")
        f(b = "b")
        /* next invocation doesn't use synthetic method: */
        f("a", "b")
    }

}