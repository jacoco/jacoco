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

/* This annotation generates on a field of type Date the following
* - a suitable implementation of getTime()
* - a suitable implementation of setTime(long)
* - a suitable implementation of before(Date)
* - a suitable implementation of after(Date)
* - a suitable implementation of compareTo(Date)
* - a suitable implementation of toInstant()
* - a suitable implementation of getTarget()
* - a suitable implementation of setTarget(Date)
*/

class GroovyDelegateClassTarget { // assertEmpty()

    @groovy.lang.Delegate
    Date target // assertEmpty()

    static void main(String[] args) {
        new GroovyDelegateClassTarget() // assertFullyCovered()
    }
}
