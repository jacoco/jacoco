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

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for callable references.
 */
object KotlinCallableReferenceTarget {

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        Since Kotlin 1.3.30
        anonymous class generated for callable reference is marked as synthetic
        https://youtrack.jetbrains.com/issue/KT-28453
        */
        nop(::main) // assertFullyCovered(0, 0)

    }

}
