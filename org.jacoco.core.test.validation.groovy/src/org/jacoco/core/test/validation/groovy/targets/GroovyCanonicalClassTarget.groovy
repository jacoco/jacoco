/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andres Almiray - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.groovy.targets

import groovy.transform.Canonical

@Canonical
class GroovyCanonicalClassTarget { // assertEmpty()

    int valRead // assertEmpty()

    static void main(String[] args) {
        new GroovyCanonicalClassTarget() // assertFullyCovered()
    }
}
