/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Nicolas Fränkel - initial API and implementation
 *    Amaury Levé - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

import org.jacoco.core.test.validation.targets.Stubs.nop

object KotlinExtensionFunctionTarget {

    private val sample = SampleClass()

    @JvmStatic
    fun main(args: Array<String>) {
        sample.extensionFunction()      // assertFullyCovered()
    }
}

private class SampleClass

private fun SampleClass.extensionFunction() {
    nop()                               // assertFullyCovered()
}