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
package org.jacoco.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class InputStreamSourceFileLocatorTest {

	private Map<String, byte[]> sources = new HashMap<String, byte[]>();

	class TestLocator extends InputStreamSourceFileLocator {

		protected TestLocator(String encoding, int tabWidth) {
			super(encoding, tabWidth);
		}

		@Override
		protected InputStream getSourceStream(String path) throws IOException {
			final byte[] bytes = sources.get(path);
			return bytes == null ? null : new ByteArrayInputStream(bytes);
		}
	}

	@Test
	public void testGetTabWidth() throws IOException {
		ISourceFileLocator locator = new TestLocator("UTF-8", 17);
		assertEquals(17, locator.getTabWidth());
	}

	@Test
	public void testGetSourceFileNegative() throws IOException {
		ISourceFileLocator locator = new TestLocator("UTF-8", 4);
		assertNull(locator.getSourceFile("org/jacoco/example", "Test.java"));
	}

	@Test
	public void testGetSourceFile() throws IOException {
		ISourceFileLocator locator = new TestLocator("UTF-8", 4);
		sources.put("org/jacoco/example/Test.java", "ÜÄö".getBytes("UTF-8"));
		assertContent("ÜÄö",
				locator.getSourceFile("org/jacoco/example", "Test.java"));
	}

	@Test
	public void testGetSourceFileDefaultPackage() throws IOException {
		ISourceFileLocator locator = new TestLocator("UTF-8", 4);
		sources.put("Test.java", "ÜÄö".getBytes("UTF-8"));
		assertContent("ÜÄö", locator.getSourceFile("", "Test.java"));
	}

	@Test
	public void testEncoding() throws IOException {
		ISourceFileLocator locator = new TestLocator("UTF-16", 4);
		sources.put("Test.java", "ÜÄö".getBytes("UTF-16"));
		assertContent("ÜÄö", locator.getSourceFile("", "Test.java"));
	}

	@Test
	public void testDefaultEncoding() throws IOException {
		ISourceFileLocator locator = new TestLocator(null, 4);
		sources.put("Test.java", "Hello World!".getBytes());
		assertContent("Hello World!", locator.getSourceFile("", "Test.java"));
	}

	private void assertContent(String expected, Reader source)
			throws IOException {
		assertNotNull(source);
		final BufferedReader buffer = new BufferedReader(source);
		assertEquals(expected, buffer.readLine());
		assertNull(buffer.readLine());
		buffer.close();
	}

}
