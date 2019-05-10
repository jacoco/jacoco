/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jan Wloka - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.groovy.targets

class GroovyDelegateClassTarget { // assertEmpty()

    @Delegate
    Date target = new Date() // assertEmpty()

    long getTime() {
        return target.time  // assertFullyCovered()
    }

    static void main(String[] args) {
        new GroovyDelegateClassTarget().time // assertFullyCovered()
    }
}
