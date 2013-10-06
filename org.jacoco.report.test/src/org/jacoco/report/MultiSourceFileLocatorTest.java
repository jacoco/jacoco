/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.Reader;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link MultiSourceFileLocator}.
 */
public class MultiSourceFileLocatorTest {

	private MultiSourceFileLocator locator;

	@Before
	public void setup() {
		locator = new MultiSourceFileLocator(7);
	}

	@Test
	public void testGetTabWidth() throws IOException {
		assertEquals(7, locator.getTabWidth());
	}

	@Test
	public void testEmpty() throws IOException {
		assertNull(locator.getSourceFile("org/jacoco/example", "Test.java"));
	}

	@Test
	public void testNohit() throws IOException {
		final StubLocator loc1 = new StubLocator();
		locator.add(loc1);
		final StubLocator loc2 = new StubLocator();
		locator.add(loc2);
		final StubLocator loc3 = new StubLocator();
		locator.add(loc3);

		assertNull(locator.getSourceFile("org/jacoco/example", "Test.java"));
	}

	@Test
	public void testHit() throws IOException {
		final StubLocator loc1 = new StubLocator();
		loc1.put("org/jacoco/example/Test1.java", "1");
		locator.add(loc1);
		final StubLocator loc2 = new StubLocator();
		loc2.put("org/jacoco/example/Test2.java", "2");
		locator.add(loc2);
		final StubLocator loc3 = new StubLocator();
		loc3.put("org/jacoco/example/Test3.java", "3");
		locator.add(loc3);

		final Reader source = locator.getSourceFile("org/jacoco/example",
				"Test2.java");
		assertNotNull(source);
		assertEquals('2', source.read());
	}

	@Test
	public void testPrecedence() throws IOException {
		final StubLocator loc1 = new StubLocator();
		loc1.put("org/jacoco/example/TestX.java", "1");
		locator.add(loc1);
		final StubLocator loc2 = new StubLocator();
		loc2.put("org/jacoco/example/Test.java", "2");
		locator.add(loc2);
		final StubLocator loc3 = new StubLocator();
		loc3.put("org/jacoco/example/Test.java", "3");
		locator.add(loc3);

		final Reader source = locator.getSourceFile("org/jacoco/example",
				"Test.java");
		assertNotNull(source);
		assertEquals('2', source.read());
	}

}
