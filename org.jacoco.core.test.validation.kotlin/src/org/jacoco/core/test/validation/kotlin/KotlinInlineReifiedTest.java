/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.kotlin;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinInlineReifiedTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinInlineReifiedTarget}.
 */
public class KotlinInlineReifiedTest extends ValidationTestBase {

	public KotlinInlineReifiedTest() {
		super(KotlinInlineReifiedTarget.class);
	}

	@Test
	public void compiler_should_generate_synthetic_method() {
		final HashSet<String> names = new HashSet<String>();
		for (final Method method : KotlinInlineReifiedTarget.class
				.getDeclaredMethods()) {
			if (method.isSynthetic()) {
				names.add(method.getName());
			}
		}

		assertEquals(Collections.singleton("example"), names);
	}

	@Test
	public void test_method_count() {
		assertMethodCount(2);
	}

}
