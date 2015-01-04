/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
