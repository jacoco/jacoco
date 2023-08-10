/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.internal.html.resources;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link Styles}.
 */
public class StylesTest {

	@Test
	public void testCombine1() {
		assertEquals(null, Styles.combine());
	}

	@Test
	public void testCombine2() {
		assertEquals(null, Styles.combine((String) null));
	}

	@Test
	public void testCombine3() {
		assertEquals("aaa", Styles.combine("aaa"));
	}

	@Test
	public void testCombine4() {
		assertEquals("aaa bbb ccc",
				Styles.combine("aaa", null, "bbb", "ccc", null));
	}

}
