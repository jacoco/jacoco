/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.report.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link NormalizedFileNames}.
 */
public class NormalizedFileNamesTest {

	private NormalizedFileNames nfn;

	@Before
	public void setup() {
		nfn = new NormalizedFileNames();
	}

	@Test
	public void testKeepLegalCharacters() {
		String id = "Foo-bar_$15.class";
		assertEquals(id, nfn.getFileName(id));
	}

	@Test
	public void testReplaceIllegalCharacters() {
		String id = "A/b C;";
		assertEquals("A_b_C_", nfn.getFileName(id));
	}

	@Test
	public void testSameInstance() {
		// If no normalization is required we should get the same instance.
		String id = new String("Example.html");
		assertSame(id, nfn.getFileName(id));
		assertSame(id, nfn.getFileName(new String("Example.html")));
	}

	@Test
	public void testReplaceIllegalCharactersNonUnique() {
		assertEquals("F__.html", nfn.getFileName("F__.html"));
		assertEquals("1~F__.html", nfn.getFileName("F**.html"));
		assertEquals("2~F__.html", nfn.getFileName("F??.html"));

		// Mapping must be reproducible
		assertEquals("F__.html", nfn.getFileName("F__.html"));
		assertEquals("1~F__.html", nfn.getFileName("F**.html"));
		assertEquals("2~F__.html", nfn.getFileName("F??.html"));
	}

	@Test
	public void testCaseAware() {
		assertEquals("Hello.html", nfn.getFileName("Hello.html"));
		assertEquals("1~HELLO.html", nfn.getFileName("HELLO.html"));
		assertEquals("2~HeLLo.html", nfn.getFileName("HeLLo.html"));

		// Mapping must be reproducible
		assertEquals("Hello.html", nfn.getFileName("Hello.html"));
		assertEquals("1~HELLO.html", nfn.getFileName("HELLO.html"));
		assertEquals("2~HeLLo.html", nfn.getFileName("HeLLo.html"));
	}

}
