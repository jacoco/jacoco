/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Stephan Classen
 *    Vadim Bauer
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.groovy.targets

import groovy.io.FileType
import groovy.transform.IndexedProperty

class GroovyIndexPropertyClassTarget { // assertEmpty()

    @IndexedProperty
    FileType[] someField  // assertEmpty()
    @IndexedProperty
    List otherField     // assertEmpty()
    @IndexedProperty
    List furtherField   // assertEmpty()

    static void main(String[] args) {
        new GroovyIndexPropertyClassTarget() // assertFullyCovered()
    }
}
