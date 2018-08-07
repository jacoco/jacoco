/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.junit.Test;

/**
 * Unit tests for {@link Source}.
 */
public class SourceTest {

	@Test
	public void testGetLines1() throws IOException {
		String src = "\na\nbb\n";
		final Source s = new Source(new StringReader(src));
		assertEquals(Arrays.asList("", "a", "bb"), s.getLines());
	}

	@Test
	public void testGetLines2() throws IOException {
		String src = "aa\nbb\n;";
		final Source s = new Source(new StringReader(src));
		assertEquals(Arrays.asList("aa", "bb", ";"), s.getLines());
	}

	@Test
	public void testGetLines3() throws IOException {
		String src = "xx\r\nyy";
		final Source s = new Source(new StringReader(src));
		assertEquals(Arrays.asList("xx", "yy"), s.getLines());
	}

	@Test
	public void testGetLine() throws IOException {
		String src = "Hello\n\nWorld!";
		final Source s = new Source(new StringReader(src));
		assertEquals("Hello", s.getLine(1));
		assertEquals("", s.getLine(2));
		assertEquals("World!", s.getLine(3));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDuplicateTag() throws IOException {
		String src = "a\nb$line-tag$\nc\nd\ne$line-tag$\nf";
		new Source(new StringReader(src));
	}

}
