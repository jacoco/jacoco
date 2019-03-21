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

import groovy.transform.builder.SimpleStrategy

/* This annotation generates the following
* - a suitable implementation of class GroovySimpleStrategyBuilderClassTarget
* - a suitable implementation of class Person
* - a suitable implementation of class GroovyBuilderClassTargetBuilder
* - a suitable implementation of GroovySimpleStrategyBuilderClassTarget()
* - a suitable implementation of Person()
* - a suitable implementation of GroovyBuilderClassTargetBuilder()
* - a suitable implementation of builder()
* - a suitable implementation of build()
* - a suitable implementation of setter method setFirst(), setLast(), setBorn()
* - a suitable implementation of getFirst()
* - a suitable implementation of getLast()
* - a suitable implementation of getBorn()
*/

@groovy.transform.builder.Builder
class GroovySimpleStrategyBuilderClassTarget { // assertEmpty()

    @groovy.transform.builder.Builder(builderStrategy= SimpleStrategy)
    class Person { // assertEmpty()
        String first
        String last
        Integer born
    }

    static void main(String[] args) {
        new GroovySimpleStrategyBuilderClassTarget() // assertFullyCovered()
    }
}
