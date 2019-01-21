/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.runtime;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestName;

/**
 * Unit test for {@link InjectedClassRuntime}.
 */
public class InjectedClassRuntimeTest extends RuntimeTestBase {

	@Rule
	public TestName testName = new TestName();

	@BeforeClass
	public static void requires_at_least_Java_9() {
		try {
			Class.forName("java.lang.Module");
		} catch (final ClassNotFoundException e) {
			throw new AssumptionViolatedException(
					"this test requires at least Java 9");
		}
	}

	@Override
	public IRuntime createRuntime() {
		return new InjectedClassRuntime(InjectedClassRuntimeTest.class,
				testName.getMethodName());
	}

	@Test
	public void startup_should_not_create_duplicate_class_definition()
			throws Exception {
		try {
			createRuntime().startup(null);
			fail("exception expected");
		} catch (final InvocationTargetException e) {
			assertTrue(e.getCause() instanceof LinkageError);
			assertTrue(e.getCause().getMessage()
					.contains("duplicate class definition"));
		}
	}

}
