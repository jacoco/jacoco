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

/* This annotation generates the following
 * - getters and setters
 * - 2 versions of addVetoablePropertyChangeListener()
 * - 2 versions of removeVetoablePropertyChangeListener()
 * - 2 versions of getVetoablePropertyChangeListeners()
 * - fireVetoablePropertyChange()
 */

@groovy.beans.Vetoable
class GroovyVetoableClassTarget { // assertEmpty()

    String firstName    // assertEmpty()
    def zipCode    // assertEmpty()

    static void main(String[] args) {
        new GroovyVetoableClassTarget() // assertFullyCovered()
    }
}
