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
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.jacoco.core.analysis.AnalyzerTest;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link RecursiveInstrumenter}.
 */
public class RecursiveInstrumenterTest {

	// no serialVersionUID to enforce calculation
	@SuppressWarnings("serial")
	public static class SerializationTarget implements Serializable {

		private final String text;

		private final int nr;

		public SerializationTarget(final String text, final int nr) {
			this.text = text;
			this.nr = nr;
		}

		@Override
		public String toString() {
			return text + nr;
		}

	}

	private SystemPropertiesRuntime runtime;

	private RecursiveInstrumenter instrumenter;

	@Before
	public void setup() throws Exception {
		runtime = new SystemPropertiesRuntime();
		instrumenter = new RecursiveInstrumenter(runtime);
		runtime.startup(new RuntimeData());
	}

	@After
	public void teardown() {
		runtime.shutdown();
	}

	private static class BrokenInputStream extends InputStream {
		@Override
		public int read() throws IOException {
			throw new IOException();
		}
	}

	@Test
	public void testInstrumentAll_Class() throws IOException {
		InputStream in = TargetLoader.getClassData(getClass());
		OutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(in, out, "Test");

		assertEquals(1, count);
	}

	@Test
	public void testInstrumentAll_Zip() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(buffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		zipout.write(TargetLoader.getClassDataAsBytes(getClass()));
		zipout.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(
				new ByteArrayInputStream(buffer.toByteArray()), out, "Test");

		assertEquals(1, count);
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				out.toByteArray()));
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	/**
	 * Triggers exception in
	 * {@link org.jacoco.core.internal.ContentTypeDetector#ContentTypeDetector(InputStream)}.
	 */
	@Test
	public void testInstrumentAll_Broken() {
		try {
			instrumenter.instrumentAll(new BrokenInputStream(),
					new ByteArrayOutputStream(), "Broken");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Broken.", e.getMessage());
		}
	}

	/**
	 * Triggers exception in
	 * {@link RecursiveInstrumenter#copy(InputStream, OutputStream, String)}.
	 */
	@Test
	public void testInstrumentAll_Broken2() {
		final InputStream inputStream = new InputStream() {
			private int count;

			@Override
			public int read() throws IOException {
				count++;
				if (count > 4) {
					throw new IOException();
				}
				return 0;
			}
		};

		try {
			instrumenter.instrumentAll(inputStream, new ByteArrayOutputStream(),
					"Broken");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Broken.", e.getMessage());
		}
	}

	/**
	 * Triggers exception in
	 * {@link RecursiveInstrumenter#nextEntry(ZipInputStream, String)}.
	 */
	@Test
	public void testInstrumentAll_BrokenZip() {
		final byte[] buffer = new byte[30];
		buffer[0] = 0x50;
		buffer[1] = 0x4b;
		buffer[2] = 0x03;
		buffer[3] = 0x04;
		Arrays.fill(buffer, 4, buffer.length, (byte) 0x42);

		try {
			instrumenter.instrumentAll(new ByteArrayInputStream(buffer),
					new ByteArrayOutputStream(), "Test.zip");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Test.zip.", e.getMessage());
		}
	}

	/**
	 * With JDK <= 6 triggers exception in
	 * {@link RecursiveInstrumenter#copy(InputStream, OutputStream, String)}.
	 *
	 * With JDK > 6 triggers exception in
	 * {@link org.jacoco.core.internal.ContentTypeDetector#ContentTypeDetector(InputStream)}.
	 */
	@Test
	public void testInstrumentAll_BrokenZipEntry() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(out);
		zip.putNextEntry(new ZipEntry("brokenentry.txt"));
		out.write(0x23); // Unexpected data here
		zip.close();

		try {
			instrumenter.instrumentAll(
					new ByteArrayInputStream(out.toByteArray()),
					new ByteArrayOutputStream(), "broken.zip");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals(
					"Error while instrumenting broken.zip@brokenentry.txt.",
					e.getMessage());
		}
	}

	@Test
	public void testInstrumentAll_BrokenClassFileInZip() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(buffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		final byte[] brokenclass = TargetLoader.getClassDataAsBytes(getClass());
		brokenclass[10] = 0x23;
		zipout.write(brokenclass);
		zipout.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			instrumenter.instrumentAll(
					new ByteArrayInputStream(buffer.toByteArray()), out,
					"test.zip");
			fail();
		} catch (IOException e) {
			assertEquals("Error while instrumenting test.zip@Test.class.",
					e.getMessage());
		}
	}

	/**
	 * Triggers exception in
	 * {@link RecursiveInstrumenter#instrumentGzip(InputStream, OutputStream, String)}.
	 */
	@Test
	public void testInstrumentAll_BrokenGZ() {
		final byte[] buffer = new byte[] { 0x1f, (byte) 0x8b, 0x00, 0x00 };

		try {
			instrumenter.instrumentAll(new ByteArrayInputStream(buffer),
					new ByteArrayOutputStream(), "Test.gz");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Test.gz.", e.getMessage());
		}
	}

	@Test
	public void testInstrumentAll_Pack200() throws IOException {
		ByteArrayOutputStream jarbuffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(jarbuffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		zipout.write(TargetLoader.getClassDataAsBytes(getClass()));
		zipout.finish();

		ByteArrayOutputStream pack200buffer = new ByteArrayOutputStream();
		GZIPOutputStream gzipOutput = new GZIPOutputStream(pack200buffer);
		Pack200.newPacker().pack(
				new JarInputStream(new ByteArrayInputStream(
						jarbuffer.toByteArray())), gzipOutput);
		gzipOutput.finish();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int count = instrumenter.instrumentAll(new ByteArrayInputStream(
				pack200buffer.toByteArray()), out, "Test");

		jarbuffer.reset();
		Pack200.newUnpacker()
				.unpack(new GZIPInputStream(new ByteArrayInputStream(
						out.toByteArray())), new JarOutputStream(jarbuffer));

		assertEquals(1, count);
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				jarbuffer.toByteArray()));
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	/**
	 * Triggers exception in
	 * {@link RecursiveInstrumenter#instrumentPack200(InputStream, OutputStream, String)}.
	 */
	@Test
	public void testInstrumentAll_BrokenPack200() {
		final byte[] buffer = new byte[] { (byte) 0xca, (byte) 0xfe,
				(byte) 0xd0, 0x0d };

		try {
			instrumenter.instrumentAll(new ByteArrayInputStream(buffer),
					new ByteArrayOutputStream(), "Test.pack200");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Test.pack200.",
					e.getMessage());
		}
	}

	@Test
	public void testInstrumentAll_Other() throws IOException {
		InputStream in = new ByteArrayInputStream("text".getBytes());
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(in, out, "Test");

		assertEquals(0, count);
		assertEquals("text", new String(out.toByteArray()));
	}

	@Test
	public void testInstrumentAll_RemoveSignatures() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(buffer);
		zipout.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
		zipout.putNextEntry(new ZipEntry("META-INF/ALIAS.SF"));
		zipout.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(
				new ByteArrayInputStream(buffer.toByteArray()), out, "Test");

		assertEquals(0, count);
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				out.toByteArray()));
		assertEquals("META-INF/MANIFEST.MF", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	@Test
	public void testInstrumentAll_KeepSignatures() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(buffer);
		zipout.putNextEntry(new ZipEntry("META-INF/ALIAS.SF"));
		zipout.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		instrumenter.setRemoveSignatures(false);
		int count = instrumenter.instrumentAll(
				new ByteArrayInputStream(buffer.toByteArray()), out, "Test");

		assertEquals(0, count);
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				out.toByteArray()));
		assertEquals("META-INF/ALIAS.SF", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

}
