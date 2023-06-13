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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.test.TargetLoader;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link Analyzer}.
 */
public class AnalyzerTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private Analyzer analyzer;

	private Map<String, IClassCoverage> classes;

	private ExecutionDataStore executionData;

	private class EmptyStructureVisitor implements ICoverageVisitor {

		public void visitCoverage(IClassCoverage coverage) {
			final String name = coverage.getName();
			assertNull("Class already processed: " + name,
					classes.put(name, coverage));
		}
	}

	@Before
	public void setup() {
		classes = new HashMap<String, IClassCoverage>();
		executionData = new ExecutionDataStore();
		analyzer = new Analyzer(executionData, new EmptyStructureVisitor());
	}

	@Test
	public void should_ignore_module_info() throws Exception {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null,
				null);
		cw.visitModule("module", 0, null).visitEnd();
		cw.visitEnd();
		final byte[] bytes = cw.toByteArray();

		analyzer.analyzeClass(bytes, "");

		assertTrue(classes.isEmpty());
	}

	@Test
	public void should_ignore_synthetic_classes() throws Exception {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V1_5, Opcodes.ACC_SYNTHETIC, "Foo", null,
				"java/lang/Object", null);
		cw.visitEnd();
		final byte[] bytes = cw.toByteArray();

		analyzer.analyzeClass(bytes, "");

		assertTrue(classes.isEmpty());
	}

	@Test
	public void should_not_modify_class_bytes_to_support_next_version()
			throws Exception {
		final byte[] originalBytes = createClass(Opcodes.V21 + 1);
		final byte[] bytes = new byte[originalBytes.length];
		System.arraycopy(originalBytes, 0, bytes, 0, originalBytes.length);
		final long expectedClassId = CRC64.classId(bytes);

		analyzer.analyzeClass(bytes, "");

		assertArrayEquals(originalBytes, bytes);
		assertEquals(expectedClassId, classes.get("Foo").getId());
	}

	private static byte[] createClass(final int version) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(version, 0, "Foo", null, "java/lang/Object", null);
		cw.visitEnd();
		return cw.toByteArray();
	}

	/**
	 * @see #analyzeAll_should_throw_exception_for_unsupported_class_file_version()
	 */
	@Test
	public void analyzeClass_should_throw_exception_for_unsupported_class_file_version() {
		final byte[] bytes = createClass(Opcodes.V21 + 2);
		try {
			analyzer.analyzeClass(bytes, "UnsupportedVersion");
			fail("exception expected");
		} catch (IOException e) {
			assertExceptionMessage("UnsupportedVersion", e);
			assertEquals("Unsupported class file major version 67",
					e.getCause().getMessage());
		}
	}

	@Test
	public void testAnalyzeClassFromStream() throws IOException {
		analyzer.analyzeClass(TargetLoader.getClassData(AnalyzerTest.class),
				"Test");
		assertClasses("org/jacoco/core/analysis/AnalyzerTest");
	}

	@Test
	public void testAnalyzeClassFromByteArray() throws IOException {
		analyzer.analyzeClass(
				TargetLoader.getClassDataAsBytes(AnalyzerTest.class), "Test");
		assertClasses("org/jacoco/core/analysis/AnalyzerTest");
		assertFalse(classes.get("org/jacoco/core/analysis/AnalyzerTest")
				.isNoMatch());
	}

	@Test
	public void testAnalyzeClassIdMatch() throws IOException {
		final byte[] bytes = TargetLoader
				.getClassDataAsBytes(AnalyzerTest.class);
		executionData.get(Long.valueOf(CRC64.classId(bytes)),
				"org/jacoco/core/analysis/AnalyzerTest", 400);
		analyzer.analyzeClass(bytes, "Test");
		assertFalse(classes.get("org/jacoco/core/analysis/AnalyzerTest")
				.isNoMatch());
	}

	@Test
	public void testAnalyzeClassNoIdMatch() throws IOException {
		executionData.get(Long.valueOf(0),
				"org/jacoco/core/analysis/AnalyzerTest", 400);
		analyzer.analyzeClass(
				TargetLoader.getClassDataAsBytes(AnalyzerTest.class), "Test");
		assertTrue(classes.get("org/jacoco/core/analysis/AnalyzerTest")
				.isNoMatch());
	}

	@Test
	public void testAnalyzeClass_Broken() throws IOException {
		final byte[] brokenclass = TargetLoader
				.getClassDataAsBytes(AnalyzerTest.class);
		brokenclass[10] = 0x23;
		try {
			analyzer.analyzeClass(brokenclass, "Broken.class");
			fail("expected exception");
		} catch (IOException e) {
			assertExceptionMessage("Broken.class", e);
		}
	}

	private static class BrokenInputStream extends InputStream {
		@Override
		public int read() throws IOException {
			throw new IOException();
		}
	}

	/**
	 * Triggers exception in {@link Analyzer#analyzeClass(InputStream, String)}.
	 */
	@Test
	public void testAnalyzeClass_BrokenStream() throws IOException {
		try {
			analyzer.analyzeClass(new BrokenInputStream(), "BrokenStream");
			fail("exception expected");
		} catch (IOException e) {
			assertExceptionMessage("BrokenStream", e);
		}
	}

	/**
	 * @see #analyzeClass_should_throw_exception_for_unsupported_class_file_version()
	 */
	@Test
	public void analyzeAll_should_throw_exception_for_unsupported_class_file_version() {
		final byte[] bytes = createClass(Opcodes.V21 + 2);
		try {
			analyzer.analyzeAll(new ByteArrayInputStream(bytes),
					"UnsupportedVersion");
			fail("exception expected");
		} catch (IOException e) {
			assertExceptionMessage("UnsupportedVersion", e);
			assertEquals("Unsupported class file major version 67",
					e.getCause().getMessage());
		}
	}

	@Test
	public void testAnalyzeAll_Class() throws IOException {
		final int count = analyzer.analyzeAll(
				TargetLoader.getClassData(AnalyzerTest.class), "Test");
		assertEquals(1, count);
		assertClasses("org/jacoco/core/analysis/AnalyzerTest");
	}

	@Test
	public void testAnalyzeAll_Zip() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(buffer);
		zip.putNextEntry(
				new ZipEntry("org/jacoco/core/analysis/AnalyzerTest.class"));
		zip.write(TargetLoader.getClassDataAsBytes(AnalyzerTest.class));
		zip.finish();
		final int count = analyzer.analyzeAll(
				new ByteArrayInputStream(buffer.toByteArray()), "Test");
		assertEquals(1, count);
		assertClasses("org/jacoco/core/analysis/AnalyzerTest");
	}

	@Test
	public void testAnalyzeAll_EmptyZipEntry() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(buffer);
		zip.putNextEntry(new ZipEntry("empty.txt"));
		zip.finish();
		final int count = analyzer.analyzeAll(
				new ByteArrayInputStream(buffer.toByteArray()), "Test");
		assertEquals(0, count);
	}

	/**
	 * Triggers exception in
	 * {@link Analyzer#analyzeAll(java.io.InputStream, String)}.
	 */
	@Test
	public void testAnalyzeAll_Broken() throws IOException {
		try {
			analyzer.analyzeAll(new BrokenInputStream(), "Test");
			fail("expected exception");
		} catch (IOException e) {
			assertExceptionMessage("Test", e);
		}
	}

	/**
	 * Triggers exception in
	 * {@link Analyzer#analyzeGzip(java.io.InputStream, String)}.
	 */
	@Test
	public void testAnalyzeAll_BrokenGZ() {
		final byte[] buffer = new byte[] { 0x1f, (byte) 0x8b, 0x00, 0x00 };
		try {
			analyzer.analyzeAll(new ByteArrayInputStream(buffer), "Test.gz");
			fail("expected exception");
		} catch (IOException e) {
			assertExceptionMessage("Test.gz", e);
		}
	}

	@Test
	public void testAnalyzeAll_Pack200() throws IOException {
		try {
			Class.forName("java.util.jar.Pack200");
		} catch (ClassNotFoundException e) {
			throw new AssumptionViolatedException(
					"this test requires JDK with Pack200");
		}

		final ByteArrayOutputStream zipbuffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(zipbuffer);
		zip.putNextEntry(
				new ZipEntry("org/jacoco/core/analysis/AnalyzerTest.class"));
		zip.write(TargetLoader.getClassDataAsBytes(AnalyzerTest.class));
		zip.finish();

		final ByteArrayOutputStream pack200buffer = new ByteArrayOutputStream();
		GZIPOutputStream gzipOutput = new GZIPOutputStream(pack200buffer);
		Pack200Streams.pack(zipbuffer.toByteArray(), gzipOutput);
		gzipOutput.finish();

		final int count = analyzer.analyzeAll(
				new ByteArrayInputStream(pack200buffer.toByteArray()), "Test");
		assertEquals(1, count);
		assertClasses("org/jacoco/core/analysis/AnalyzerTest");
	}

	/**
	 * Triggers exception in
	 * {@link Analyzer#analyzePack200(java.io.InputStream, String)}.
	 */
	@Test
	public void testAnalyzeAll_BrokenPack200() {
		final byte[] buffer = new byte[] { (byte) 0xca, (byte) 0xfe,
				(byte) 0xd0, 0x0d };
		try {
			analyzer.analyzeAll(new ByteArrayInputStream(buffer),
					"Test.pack200");
			fail("expected exception");
		} catch (IOException e) {
			assertExceptionMessage("Test.pack200", e);
		}
	}

	@Test
	public void testAnalyzeAll_Empty() throws IOException {
		final int count = analyzer
				.analyzeAll(new ByteArrayInputStream(new byte[0]), "Test");
		assertEquals(0, count);
		assertEquals(Collections.emptyMap(), classes);
	}

	@Test
	public void testAnalyzeAll_Folder() throws IOException {
		createClassfile("bin1", AnalyzerTest.class);
		final int count = analyzer.analyzeAll(folder.getRoot());
		assertEquals(1, count);
		assertClasses("org/jacoco/core/analysis/AnalyzerTest");
	}

	@Test
	public void testAnalyzeAll_Path() throws IOException {
		createClassfile("bin1", Analyzer.class);
		createClassfile("bin2", AnalyzerTest.class);
		String path = "bin1" + File.pathSeparator + "bin2";
		final int count = analyzer.analyzeAll(path, folder.getRoot());
		assertEquals(2, count);
		assertClasses("org/jacoco/core/analysis/Analyzer",
				"org/jacoco/core/analysis/AnalyzerTest");
	}

	/**
	 * Triggers exception in
	 * {@link Analyzer#nextEntry(java.util.zip.ZipInputStream, String)}.
	 */
	@Test
	public void testAnalyzeAll_BrokenZip() {
		final byte[] buffer = new byte[30];
		buffer[0] = 0x50;
		buffer[1] = 0x4b;
		buffer[2] = 0x03;
		buffer[3] = 0x04;
		Arrays.fill(buffer, 4, buffer.length, (byte) 0x42);
		try {
			analyzer.analyzeAll(new ByteArrayInputStream(buffer), "Test.zip");
			fail("expected exception");
		} catch (IOException e) {
			assertExceptionMessage("Test.zip", e);
		}
	}

	/**
	 * With JDK 5 triggers exception in
	 * {@link Analyzer#nextEntry(ZipInputStream, String)}, i.e. message will
	 * contain only "broken.zip".
	 *
	 * With JDK > 5 triggers exception in
	 * {@link Analyzer#analyzeAll(java.io.InputStream, String)}, i.e. message
	 * will contain only "broken.zip@brokenentry.txt".
	 */
	@Test
	public void testAnalyzeAll_BrokenZipEntry() throws IOException {
		File file = new File(folder.getRoot(), "broken.zip");
		OutputStream out = new FileOutputStream(file);
		ZipOutputStream zip = new ZipOutputStream(out);
		zip.putNextEntry(new ZipEntry("brokenentry.txt"));
		out.write(0x23); // Unexpected data here
		zip.close();
		try {
			analyzer.analyzeAll(file);
			fail("expected exception");
		} catch (IOException e) {
			assertTrue(e.getMessage().startsWith("Error while analyzing"));
			assertTrue(e.getMessage().contains("broken.zip"));
		}
	}

	/**
	 * Triggers exception in
	 * {@link Analyzer#analyzeClass(java.io.InputStream, String)}.
	 */
	@Test
	public void testAnalyzeAll_BrokenClassFileInZip() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(buffer);
		zip.putNextEntry(
				new ZipEntry("org/jacoco/core/analysis/AnalyzerTest.class"));
		final byte[] brokenclass = TargetLoader
				.getClassDataAsBytes(AnalyzerTest.class);
		brokenclass[10] = 0x23;
		zip.write(brokenclass);
		zip.finish();

		try {
			analyzer.analyzeAll(new ByteArrayInputStream(buffer.toByteArray()),
					"test.zip");
			fail("expected exception");
		} catch (IOException e) {
			assertExceptionMessage(
					"test.zip@org/jacoco/core/analysis/AnalyzerTest.class", e);
		}
	}

	private void createClassfile(final String dir, final Class<?> source)
			throws IOException {
		File file = new File(folder.getRoot(), dir);
		file.mkdirs();
		file = new File(file, "some.class");
		OutputStream out = new FileOutputStream(file);
		out.write(TargetLoader.getClassDataAsBytes(source));
		out.close();
	}

	private void assertClasses(String... classNames) {
		assertEquals(new HashSet<String>(Arrays.asList(classNames)),
				classes.keySet());
	}

	private void assertExceptionMessage(String name, Exception ex) {
		String expected = "Error while analyzing " + name + " with JaCoCo "
				+ JaCoCo.VERSION + "/" + JaCoCo.COMMITID_SHORT + ".";
		assertEquals(expected, ex.getMessage());
	}

}
