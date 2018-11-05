/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andres Almiray - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.groovy;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.groovy.targets.GroovyDataClassTarget;
import org.junit.Test;

/**
 * Test of <code>data class</code>es.
 */
public class GroovyDataClassTest extends ValidationTestBase {
    public GroovyDataClassTest() {
        super(GroovyDataClassTarget.class);
    }

    @Test
    public void test_method_count() {
        // This test fails in Groovy 2.5.2 and earlier because the
        // compiler does not at @groovy.transform.Generated to
        // property getters & setters nor the @ToString and
        // @EqualsAndHashcode AST transformations
        assertMethodCount(2);
    }
}
