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
package org.jacoco.core.test.validation.groovy;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.groovy.targets.GroovySimpleStrategyBuilderClassTarget;
import org.junit.Test;

/**
 * Test of <code>builder annotation</code>s.
 */
public class GroovySimpleStrategyBuilderClassTest extends ValidationTestBase {
    public GroovySimpleStrategyBuilderClassTest() {
        super(GroovySimpleStrategyBuilderClassTarget.class);
    }

    @Test
    public void test_method_count() {
        assertMethodCount(1);
    }
}
