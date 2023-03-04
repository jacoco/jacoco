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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link AntFilesLocator}.
 */
public class AntFilesLocatorTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private AntFilesLocator locator;

	@Before
	public void setup() {
		locator = new AntFilesLocator("UTF-8", 4);
	}

	@Test
	public void testGetSourceFileNegative() throws IOException {
		assertNull(locator.getSourceFile("org/jacoco/somewhere",
				"DoesNotExist.java"));
	}

	@Test
	public void testGetSourceFile() throws IOException {
		locator.add(createFile("org/jacoco/example/Test.java"));
		final Reader source = locator.getSourceFile("org/jacoco/example",
				"Test.java");
		assertContent(source);
	}

	private Resource createFile(String path) throws IOException {
		final File file = new File(folder.getRoot(), path);
		file.getParentFile().mkdirs();
		final Writer writer = new OutputStreamWriter(new FileOutputStream(file),
				"UTF-8");
		writer.write("Source");
		writer.close();
		return new FileResource(folder.getRoot(), path);
	}

	private void assertContent(Reader source) throws IOException {
		assertNotNull(source);
		final BufferedReader buffer = new BufferedReader(source);
		assertEquals("Source", buffer.readLine());
		assertNull(buffer.readLine());
		buffer.close();
	}

}
