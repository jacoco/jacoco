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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link DirectorySourceFileLocator}.
 */
public class DirectorySourceFileLocatorTest {

	@Rule
	public final TemporaryFolder sourceFolder = new TemporaryFolder();

	private ISourceFileLocator locator;

	@Before
	public void setup() {
		locator = new DirectorySourceFileLocator(sourceFolder.getRoot(),
				"UTF-8", 4);
	}

	@Test
	public void getSourceFile_should_return_null_when_source_does_not_exist()
			throws IOException {
		assertNull(locator.getSourceFile("org/jacoco/example",
				"DoesNotExist.java"));
	}

	@Test
	public void getSourceFile_should_return_null_when_source_is_folder()
			throws IOException {
		final File file = new File(sourceFolder.getRoot(),
				"org/jacoco/example");
		file.mkdirs();
		assertNull(locator.getSourceFile("org/jacoco", "example"));
	}

	@Test
	public void getSourceFile_should_return_content_when_file_exists()
			throws IOException {
		createFile("org/jacoco/example/Test.java");
		final Reader source = locator.getSourceFile("org/jacoco/example",
				"Test.java");
		assertContent(source);
	}

	private void createFile(String path) throws IOException {
		final File file = new File(sourceFolder.getRoot(), path);
		file.getParentFile().mkdirs();
		final Writer writer = new OutputStreamWriter(new FileOutputStream(file),
				"UTF-8");
		writer.write("Source");
		writer.close();
	}

	private void assertContent(Reader source) throws IOException {
		assertNotNull(source);
		final BufferedReader buffer = new BufferedReader(source);
		assertEquals("Source", buffer.readLine());
		assertNull(buffer.readLine());
		buffer.close();
	}

}
