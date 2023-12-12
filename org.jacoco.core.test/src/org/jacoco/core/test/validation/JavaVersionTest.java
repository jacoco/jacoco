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
package org.jacoco.core.test.validation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JavaVersionTest {

	@Test
	public void should_parse_values_of_java_version_property() {
		JavaVersion v;

		v = new JavaVersion("1.8.0_162");
		assertEquals(8, v.feature());
		assertEquals(162, v.update());

		v = new JavaVersion("1.8.0_172-ea");
		assertEquals(8, v.feature());
		assertEquals(172, v.update());

		v = new JavaVersion("9");
		assertEquals(9, v.feature());
		assertEquals(0, v.update());

		v = new JavaVersion("9.0.1");
		assertEquals(9, v.feature());
		assertEquals(1, v.update());

		v = new JavaVersion("10-ea");
		assertEquals(10, v.feature());
		assertEquals(0, v.update());
	}

	@Test
	public void should_compare_with_given_version() {
		assertTrue(new JavaVersion("1.7.0_80").isBefore("1.8.0_92"));

		assertTrue(new JavaVersion("1.8.0_31").isBefore("1.8.0_92"));

		assertFalse(new JavaVersion("1.8.0_92").isBefore("1.8.0_92"));

		assertFalse(new JavaVersion("1.8.0_162").isBefore("1.8.0_92"));
		assertFalse(new JavaVersion("1.8.0_162").isBefore("1.8"));

		assertFalse(new JavaVersion("9.0.1").isBefore("1.8.0_92"));
	}

}
