/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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
	public void testGetSourceFileNegative() throws IOException {
		assertNull(locator.getSourceFile("org/jacoco/example",
				"DoesNotExist.java"));
	}

	@Test
	public void testGetSourceFile() throws IOException {
		createFile("org/jacoco/example/Test.java");
		final Reader source = locator.getSourceFile("org/jacoco/example",
				"Test.java");
		assertContent(source);
	}

	private void createFile(String path) throws IOException {
		final File file = new File(sourceFolder.getRoot(), path);
		file.getParentFile().mkdirs();
		final Writer writer = new OutputStreamWriter(
				new FileOutputStream(file), "UTF-8");
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
