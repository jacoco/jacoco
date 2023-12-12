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
