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

import groovy.transform.Sortable

/* This annotation generates the following
 * - implement the Comparable interface
 * - have a compareTo method based on the first, last and born properties (priority ordering will be according to the ordering of property definition, highest first, unless 'includes' is used; in which case, priority will be according to the order given in the includes list)
 * - have three Comparator methods named comparatorByFirst, comparatorByLast and comparatorByBorn
 * - sort by natural order by default, reversed natural order can be specified
 */

@Sortable
class GroovySortableClassTarget { // assertEmpty()

    String first    // assertEmpty()
    String last     // assertEmpty()
    Integer born    // assertEmpty()

    static void main(String[] args) {
        new GroovySortableClassTarget() // assertFullyCovered()
    }
}
