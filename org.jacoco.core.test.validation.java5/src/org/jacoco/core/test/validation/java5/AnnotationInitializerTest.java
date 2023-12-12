/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.AnnotationInitializerTarget;

/**
 * Test of initializer in annotations.
 */
public class AnnotationInitializerTest extends ValidationTestBase {

	public AnnotationInitializerTest() {
		super(AnnotationInitializerTarget.class);
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

}
