/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.test.validation.targets.Stubs.f
import org.jacoco.core.test.validation.targets.Stubs.i2
import org.jacoco.core.test.validation.targets.Stubs.i3
import org.jacoco.core.test.validation.targets.Stubs.nop
import org.jacoco.core.test.validation.targets.Stubs.t

object KotlinBranchesTarget {

    @JvmStatic
    fun main(args: Array<String>) {
        if (f()) { // assertCoveredBranches("{1}")
            nop()
        }
        if (t()) { // assertCoveredBranches("{0}")
            nop()
        }

        if (f() || f()) { // assertCoveredBranches("{0, 3}")
            nop()
        }
        if (f() || t()) { // assertCoveredBranches("{0, 2}")
            nop()
        }
        if (t() || f()) { // assertCoveredBranches("{1}")
            nop()
        }
        if (t() || t()) { // assertCoveredBranches("{1}")
            nop()
        }

        if (f() && f()) { // assertCoveredBranches("{1}")
            nop()
        }
        if (f() && t()) { // assertCoveredBranches("{1}")
            nop()
        }
        if (t() && f()) { // assertCoveredBranches("{0, 3}")
            nop()
        }
        if (t() && t()) { // assertCoveredBranches("{0, 2}")
            nop()
        }

        when (i3()) { // assertCoveredBranches("{0}")
            0 -> nop()
            1 -> nop()
            2 -> nop()
            else -> nop()
        }
        when (i2()) { // assertCoveredBranches("{3}")
            0 -> nop()
            1 -> nop()
            2 -> nop()
            else -> nop()
        }
    }

}
