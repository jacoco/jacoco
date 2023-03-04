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
package org.jacoco.core.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.SessionInfoStore;
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
	public void testLoadFile() throws IOException {
		loader.load(createFile("a"));
		loader.load(createFile("bb"));

		assertLoaderContents("a", "bb");
	}

	@Test
	public void testLoadInputStream() throws IOException {
		final FileInputStream in1 = new FileInputStream(createFile("a"));
		loader.load(in1);
		in1.close();
		final FileInputStream in2 = new FileInputStream(createFile("bb"));
		loader.load(in2);
		in2.close();

		assertLoaderContents("a", "bb");
	}

	@Test(expected = IOException.class)
	public void testLoadBrokenContent() throws IOException {
		final File file = new File(sourceFolder.getRoot(), "broken.exec");
		final FileWriter writer = new FileWriter(file);
		writer.write("Invalid Content");
		writer.close();

		loader.load(file);
	}

	@Test
	public void testSaveFile() throws IOException {
		final File file = new File(sourceFolder.getRoot(), "target.exec");

		// Write invalid data to ensure the file is actually overwritten:
		final OutputStream out = new FileOutputStream(file);
		out.write("invalid".getBytes());
		out.close();

		loader.load(createFile("a"));
		loader.save(file, false);

		assertFileContents(file, "a");
	}

	@Test
	public void testSaveFileAppend() throws IOException {
		final File file = createFile("a");

		loader.load(createFile("bb"));
		loader.save(file, true);

		assertFileContents(file, "a", "bb");
	}

	@Test
	public void testCreateSubfolders() throws IOException {
		final File file = new File(sourceFolder.getRoot(), "a/b/c/target.exec");

		loader.load(createFile("a"));
		loader.save(file, true);

		assertFileContents(file, "a");
	}

	private File createFile(String id) throws IOException {
		final File file = new File(sourceFolder.getRoot(), id + ".exec");
		final FileOutputStream out = new FileOutputStream(file);
		final ExecutionDataWriter writer = new ExecutionDataWriter(out);
		final int value = id.length();
		writer.visitClassExecution(
				new ExecutionData(value, id, new boolean[] { true }));
		writer.visitSessionInfo(new SessionInfo(id, value, value));
		out.close();
		return file;
	}

	private void assertLoaderContents(String... expected) {
		assertContents(loader.getExecutionDataStore(),
				loader.getSessionInfoStore(), expected);
	}

	private void assertFileContents(File file, String... expected)
			throws IOException {
		final InputStream in = new FileInputStream(file);
		final ExecutionDataStore execStore = new ExecutionDataStore();
		final SessionInfoStore sessionStore = new SessionInfoStore();
		final ExecutionDataReader reader = new ExecutionDataReader(in);
		reader.setExecutionDataVisitor(execStore);
		reader.setSessionInfoVisitor(sessionStore);
		reader.read();
		assertContents(execStore, sessionStore, expected);
	}

	private void assertContents(ExecutionDataStore execStore,
			SessionInfoStore sessionStore, String... expected) {
		final List<SessionInfo> infos = sessionStore.getInfos();

		assertEquals(expected.length, execStore.getContents().size());
		assertEquals(expected.length, infos.size());
		int idx = 0;
		for (String id : expected) {
			assertEquals(id, execStore.get(id.length()).getName());
			assertEquals(id, infos.get(idx++).getId());
		}
	}

}
