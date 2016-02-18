/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.internal.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ValidationUtilTest {

	private final String name;
	private final Object value;
	private final String expectedExceptionMessage;

	@Parameters
	public static Collection<?> data() {
		return Arrays.asList(new Object[][] { //
				{ "name", new Object(), null }, //
						{ null, "value", null }, //
						{ "name", null, "name is null" }, //
						{ null, null, "null is null" } //
				});
	}

	@Test
	public void testDefaultConstructor() throws Exception {
		Constructor<ValidationUtils> constructor = ValidationUtils.class
				.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
		// Does not throw is all we test
	}

	public ValidationUtilTest(final String name, final Object value,
			final String expectedExceptionMessage) {
		this.name = name;
		this.value = value;
		this.expectedExceptionMessage = expectedExceptionMessage;
	}

	@Test
	public void validateNotNull() {
		String actualExceptionMessage = null;
		try {
			ValidationUtils.validateNotNull(name, value);
		} catch (IllegalArgumentException e) {
			actualExceptionMessage = e.getMessage();
		}
		assertEquals(expectedExceptionMessage, actualExceptionMessage);
	}
}