/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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

    class Example<T : Any> {
        lateinit var publicMember: String // assertFullyCovered()
        lateinit var publicGenericMember: T // assertFullyCovered()
        private lateinit var privateMember: String // assertEmpty()
        private lateinit var privateGenericMember: T // assertEmpty()

        fun init(value: T) {
            publicMember = ""
            publicGenericMember = value
            privateMember = ""
            privateGenericMember = value
        }

        fun readPublic() {
            nop(publicMember) // assertFullyCovered()
        }

        fun readPublicGeneric() {
            nop(publicGenericMember) // assertFullyCovered()
        }

        fun readPrivate() {
            nop(privateMember) // assertFullyCovered()
        }

        fun readPrivateGeneric() {
            nop(privateGenericMember) // assertFullyCovered()
        }
    }

    fun readPublicOutside(e: Example<String>) {
        nop(e.publicMember) // assertFullyCovered()
    }

    fun readPublicGenericOutside(e: Example<String>) {
        nop(e.publicGenericMember) // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val e = Example<String>()
        e.init("")
        e.readPublic()
        e.readPublicGeneric()
        e.readPrivate()
        e.readPrivateGeneric()
        readPublicOutside(e)
        readPublicGenericOutside(e)
    }

}
