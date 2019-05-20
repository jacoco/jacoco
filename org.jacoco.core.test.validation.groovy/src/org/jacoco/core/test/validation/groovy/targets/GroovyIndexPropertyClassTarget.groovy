/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Classen
 *    Vadim Bauer
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.groovy.targets

import groovy.io.FileType
import groovy.transform.IndexedProperty

/* This annotation generates the following
 * for all annotated fields
 * - standard getter and setter
 * - getter and setter with an index
 *      - FieldType getSomeField(int index)
 *      - void setSomeField(int index, FieldType val)
 */

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
