/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ShortenedPackageSourceFileLocator}.
 */
public class ShortenedPackageSourceFileLocatorTest
		implements ISourceFileLocator {

	private ISourceFileLocator target;

	private Map<String, String> sourceFiles;

	@Before
	public void setup() {
		target = new ShortenedPackageSourceFileLocator(this);
		sourceFiles = new HashMap<String, String>();
	}

	@Test
	public void getTabWidth_should_return_value_from_delegate() {
		assertEquals(42, target.getTabWidth());
	}

	@Test
	public void getSourceFile_should_return_null_when_source_does_not_exist()
			throws IOException {
		assertNull(target.getSourceFile("org/jacoco/example", "Foo.java"));
	}

	@Test
	public void getSourceFile_should_return_source_on_exact_match()
			throws IOException {
		addSource("org/jacoco/example", "Foo.java", "class Foo");
		assertContent("class Foo",
				target.getSourceFile("org/jacoco/example", "Foo.java"));
	}

	@Test
	public void getSourceFile_should_prefer_exact_match() throws IOException {
		addSource("org/jacoco/example", "Foo.java", "class Foo");
		addSource("jacoco/example", "Foo.java", "Foo in jacoco/example");
		assertContent("class Foo",
				target.getSourceFile("org/jacoco/example", "Foo.java"));
	}

	@Test
	public void getSourceFile_should_return_source_from_shortened_package_1_segment()
			throws IOException {
		addSource("jacoco/example", "Foo.java", "class Foo");
		assertContent("class Foo",
				target.getSourceFile("org/jacoco/example", "Foo.java"));
	}

	@Test
	public void getSourceFile_should_return_source_from_shortened_package_2_segments()
			throws IOException {
		addSource("example", "Foo.java", "class Foo");
		assertContent("class Foo",
				target.getSourceFile("org/jacoco/example", "Foo.java"));
	}

	@Test
	public void getSourceFile_should_return_source_from_shortened_package_all_segments()
			throws IOException {
		addSource("", "Foo.java", "class Foo");
		assertContent("class Foo",
				target.getSourceFile("org/jacoco/example", "Foo.java"));
	}

	private void assertContent(String expected, Reader reader)
			throws IOException {
		assertNotNull(reader);
		StringBuilder buffer = new StringBuilder();
		int c;
		while ((c = reader.read()) != -1) {
			buffer.append((char) c);
		}
		assertEquals(expected, buffer.toString());
	}

	// === delegate mock ===

	public Reader getSourceFile(String packageName, String fileName)
			throws IOException {
		String content = sourceFiles.get(packageName + "#" + fileName);
		return content == null ? null : new StringReader(content);
	}

	public int getTabWidth() {
		return 42;
	}

	private void addSource(String packageName, String fileName,
			String content) {
		sourceFiles.put(packageName + "#" + fileName, content);
	}

}
