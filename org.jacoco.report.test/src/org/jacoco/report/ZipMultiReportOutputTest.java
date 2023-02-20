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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ZipMultiReportOutput}.
 */
public class ZipMultiReportOutputTest {

	private ByteArrayOutputStream buffer;

	private ZipMultiReportOutput zipOutput;

	@Before
	public void setup() throws Exception {
		buffer = new ByteArrayOutputStream();
		zipOutput = new ZipMultiReportOutput(buffer);
	}

	@Test
	public void testWrite1() throws IOException {
		final byte[] content1 = "HelloZip".getBytes();

		OutputStream out = zipOutput.createFile("a.txt");
		out.write(content1);
		out.close();

		zipOutput.close();

		final Map<String, byte[]> entries = readEntries();
		assertEquals(Collections.singleton("a.txt"), entries.keySet());
		assertArrayEquals(content1, entries.get("a.txt"));
	}

	@Test
	public void testWrite2() throws IOException {
		final byte[] content1 = "HelloZip".getBytes("ISO-8859-1");

		OutputStream out = zipOutput.createFile("b.txt");
		out.write(content1, 5, 3);
		out.close();

		zipOutput.close();

		final Map<String, byte[]> entries = readEntries();
		assertEquals(Collections.singleton("b.txt"), entries.keySet());
		assertArrayEquals("Zip".getBytes("ISO-8859-1"), entries.get("b.txt"));
	}

	@Test
	public void testWrite3() throws IOException {

		OutputStream out = zipOutput.createFile("b.txt");
		out.write(40);
		out.flush();
		out.close();

		zipOutput.close();

		final Map<String, byte[]> entries = readEntries();
		assertEquals(Collections.singleton("b.txt"), entries.keySet());
		assertArrayEquals(new byte[] { 40 }, entries.get("b.txt"));
	}

	@Test
	public void testCreateFiles() throws IOException {
		final byte[] content1 = "HelloZip".getBytes();

		OutputStream out = zipOutput.createFile("dir/index.html");
		out.write(content1);
		out.close();

		final byte[] content2 = "HelloWorld".getBytes();

		out = zipOutput.createFile("readme.txt");
		out.write(content2);
		out.close();

		zipOutput.close();

		final Map<String, byte[]> entries = readEntries();
		assertEquals(
				new HashSet<String>(
						Arrays.asList("dir/index.html", "readme.txt")),
				entries.keySet());
		assertArrayEquals(content1, entries.get("dir/index.html"));
		assertArrayEquals(content2, entries.get("readme.txt"));
	}

	@Test
	public void testCreateFilesWithoutClose() throws IOException {
		final byte[] content1 = "HelloZip".getBytes();

		OutputStream out = zipOutput.createFile("dir/index.html");
		out.write(content1);

		final byte[] content2 = "HelloWorld".getBytes();

		out = zipOutput.createFile("readme.txt");
		out.write(content2);

		zipOutput.close();

		final Map<String, byte[]> entries = readEntries();
		assertEquals(
				new HashSet<String>(
						Arrays.asList("dir/index.html", "readme.txt")),
				entries.keySet());
		assertArrayEquals(content1, entries.get("dir/index.html"));
		assertArrayEquals(content2, entries.get("readme.txt"));
	}

	@Test(expected = IOException.class)
	public void testWriteToClosedStream1() throws IOException {
		OutputStream out = zipOutput.createFile("index.html");
		out.close();
		out.write("HelloZip".getBytes());
	}

	@Test(expected = IOException.class)
	public void testWriteToClosedStream2() throws IOException {
		OutputStream out = zipOutput.createFile("index.html");
		out.close();
		out.write("HelloZip".getBytes(), 2, 3);
	}

	@Test(expected = IOException.class)
	public void testWriteToClosedStream3() throws IOException {
		OutputStream out = zipOutput.createFile("index.html");
		out.close();
		out.write(32);
	}

	@Test(expected = IOException.class)
	public void testFlushToClosedStream3() throws IOException {
		OutputStream out = zipOutput.createFile("index.html");
		out.close();
		out.flush();
	}

	@Test(expected = IOException.class)
	public void testWriteToObsoleteStream() throws IOException {
		final OutputStream out1 = zipOutput.createFile("a.txt");
		zipOutput.createFile("b.txt");
		out1.write(32);
	}

	private Map<String, byte[]> readEntries() throws IOException {
		final Map<String, byte[]> entries = new HashMap<String, byte[]>();
		final byte[] bytes = buffer.toByteArray();
		final ZipInputStream input = new ZipInputStream(
				new ByteArrayInputStream(bytes));
		ZipEntry entry;
		while ((entry = input.getNextEntry()) != null) {
			final ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
			int b;
			while ((b = input.read()) != -1) {
				entryBuffer.write(b);
			}
			byte[] old = entries.put(entry.getName(),
					entryBuffer.toByteArray());
			assertNull("Duplicate entry " + entry.getName(), old);
		}
		return entries;
	}

}
