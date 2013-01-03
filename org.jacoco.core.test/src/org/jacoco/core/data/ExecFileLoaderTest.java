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
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link ExecFileLoader}.
 */
public class ExecFileLoaderTest {

	@Rule
	public final TemporaryFolder sourceFolder = new TemporaryFolder();

	private ExecFileLoader loader;

	@Before
	public void setup() {
		loader = new ExecFileLoader();
	}

	@Test
	public void testFile() throws IOException {
		loader.load(createFile("a"));
		loader.load(createFile("bb"));

		assertContents();
	}

	@Test
	public void testInputStream() throws IOException {
		final FileInputStream in1 = new FileInputStream(createFile("a"));
		loader.load(in1);
		in1.close();
		final FileInputStream in2 = new FileInputStream(createFile("bb"));
		loader.load(in2);
		in2.close();

		assertContents();
	}

	@Test(expected = IOException.class)
	public void testBrokenContent() throws IOException {
		final File file = new File(sourceFolder.getRoot(), "broken.exec");
		final FileWriter writer = new FileWriter(file);
		writer.write("Invalid Content");
		writer.close();

		loader.load(file);
	}

	private File createFile(String id) throws IOException {
		final File file = new File(sourceFolder.getRoot(), id + ".exec");
		final FileOutputStream out = new FileOutputStream(file);
		final ExecutionDataWriter writer = new ExecutionDataWriter(out);
		final int value = id.length();
		writer.visitClassExecution(new ExecutionData(value, id, new boolean[0]));
		writer.visitSessionInfo(new SessionInfo(id, value, value));
		out.close();
		return file;
	}

	private void assertContents() {
		final ExecutionDataStore executionData = loader.getExecutionDataStore();
		final SessionInfoStore sessionInfos = loader.getSessionInfoStore();

		assertEquals(2, executionData.getContents().size());
		assertEquals("a", executionData.get(1).getName());
		assertEquals("bb", executionData.get(2).getName());

		final List<SessionInfo> infos = sessionInfos.getInfos();
		assertEquals(2, infos.size());
		assertEquals("a", infos.get(0).getId());
		assertEquals("bb", infos.get(1).getId());
	}

}
