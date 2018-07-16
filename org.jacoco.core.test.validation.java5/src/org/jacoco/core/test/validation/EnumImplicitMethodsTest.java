/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.targets.EnumImplicitMethods;
import org.junit.Test;

/**
 * Test of an implicit methods and static initializer in enums.
 */
public class EnumImplicitMethodsTest extends ValidationTestBase {

    public EnumImplicitMethodsTest() {
        super(EnumImplicitMethods.class);
    }

    @Test
    public void testCoverageResult() {
        assertMethodCount(5);

        assertLine("classdef", ICounter.FULLY_COVERED);
        assertLine("customValueOfMethod", ICounter.NOT_COVERED);
        assertLine("customValuesMethod", ICounter.NOT_COVERED);

        assertLine("const", ICounter.PARTLY_COVERED);
        assertLine("staticblock", ICounter.FULLY_COVERED);
        assertLine("super", ICounter.FULLY_COVERED);
        assertLine("constructor", ICounter.FULLY_COVERED);
    }

}
