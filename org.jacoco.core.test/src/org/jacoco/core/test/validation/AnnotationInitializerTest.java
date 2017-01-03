/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.test.validation.targets.AnnotationInitializer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test of initializer in annotations.
 */
public class AnnotationInitializerTest extends ValidationTestBase {

	public AnnotationInitializerTest() {
		super(AnnotationInitializer.class);
	}

	@Override
	protected void run(Class<?> targetClass) throws Exception {
		// Instrumentation should not add members,
		// otherwise sun.reflect.annotation.AnnotationInvocationHandler
		// can throw java.lang.annotation.AnnotationFormatError
		assertEquals(1, targetClass.getDeclaredFields().length);
		assertEquals(1, targetClass.getDeclaredMethods().length);

		// Force initialization
		targetClass.getField("CONST").get(null);
	}

	@Test
	public void testCoverageResult() {
		assertLine("const", ICounter.FULLY_COVERED);
		assertLine("value", ICounter.EMPTY);
	}

}
