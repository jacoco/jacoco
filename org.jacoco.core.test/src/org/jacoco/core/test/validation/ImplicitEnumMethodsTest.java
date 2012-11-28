/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hare Robertson - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation;

import java.lang.reflect.Field;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.targets.Target11;
import org.junit.Test;

public class ImplicitEnumMethodsTest extends ValidationTestBase {

	public ImplicitEnumMethodsTest() {
		super(Target11.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		// Load one of the enum constants
		Field fieldA = targetClass.getField("A");
		fieldA.get(targetClass);
	}

	@Test
	public void testCoverageResult() {

		assertLine("classdef", ICounter.FULLY_COVERED);

	}

}
