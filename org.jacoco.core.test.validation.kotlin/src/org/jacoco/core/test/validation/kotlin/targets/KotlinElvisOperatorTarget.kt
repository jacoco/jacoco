/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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
 * Test target for elvis operator.
 */
object KotlinElvisOperatorTarget {

    private fun example(x: String?): String {
        return x ?: "" // assertFullyCovered(0, 2)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        example("")
        example(null)
    }

}
