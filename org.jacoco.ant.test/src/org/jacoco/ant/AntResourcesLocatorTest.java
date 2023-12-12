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
package org.jacoco.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link AntResourcesLocator}.
 */
public class AntResourcesLocatorTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private AntResourcesLocator locator;

	@Before
	public void setup() {
		locator = new AntResourcesLocator("UTF-8", 8);
	}

	@Test
	public void testGetTabWidth() {
		assertEquals(8, locator.getTabWidth());
	}

	@Test
	public void testEmpty() {
		assertTrue(locator.isEmpty());
	}

	@Test
	public void testFile() throws IOException {
		locator.add(createFile("org/jacoco/example/Test.java", "AAA"));

		assertFalse(locator.isEmpty());
		final Reader source = locator.getSourceFile("org/jacoco/example",
				"Test.java");
		assertContent("AAA", source);
	}

	@Test
	public void testDirectory() throws IOException {
		createFile("src/org/jacoco/example/Test.java", "AAA");
		locator.add(new FileResource(folder.getRoot(), "src"));

		assertFalse(locator.isEmpty());
		final Reader source = locator.getSourceFile("org/jacoco/example",
				"Test.java");
		assertContent("AAA", source);
	}

	@Test
	public void testFilePrecedence() throws IOException {
		createFile("src/org/jacoco/example/Test.java", "DDD");
		locator.add(new FileResource(folder.getRoot(), "src"));
		locator.add(createFile("org/jacoco/example/Test.java", "FFF"));

		final Reader source = locator.getSourceFile("org/jacoco/example",
				"Test.java");
		assertContent("FFF", source);
	}

	@Test
	public void testDirectoryOrdering() throws IOException {
		createFile("src1/org/jacoco/example/Test.java", "AAA");
		locator.add(new FileResource(folder.getRoot(), "src1"));
		createFile("src2/org/jacoco/example/Test.java", "BBB");
		locator.add(new FileResource(folder.getRoot(), "src2"));
		createFile("src3/org/jacoco/example/Test.java", "CCC");
		locator.add(new FileResource(folder.getRoot(), "src3"));

		final Reader source = locator.getSourceFile("org/jacoco/example",
				"Test.java");
		assertContent("AAA", source);
	}

	@Test
	public void testAddAll() throws IOException {
		List<Resource> resources = new ArrayList<Resource>();
		resources.add(createFile("org/jacoco/example/Test1.java", "AAA"));
		resources.add(createFile("org/jacoco/example/Test2.java", "BBB"));
		locator.addAll(resources.iterator());

		assertFalse(locator.isEmpty());
		Reader source = locator.getSourceFile("org/jacoco/example",
				"Test1.java");
		assertContent("AAA", source);
		source = locator.getSourceFile("org/jacoco/example", "Test2.java");
		assertContent("BBB", source);
	}

	private Resource createFile(String path, String content)
			throws IOException {
		final File file = new File(folder.getRoot(), path);
		file.getParentFile().mkdirs();
		final Writer writer = new OutputStreamWriter(new FileOutputStream(file),
				"UTF-8");
		writer.write(content);
		writer.close();
		return new FileResource(folder.getRoot(), path);
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
