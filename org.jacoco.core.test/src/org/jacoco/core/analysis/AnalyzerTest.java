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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link Analyzer}.
 */
public class AnalyzerTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private Analyzer analyzer;

	private final Set<String> classes = new HashSet<String>();

	private class EmptyStructureVisitor implements ICoverageVisitor {

		public void visitCoverage(IClassCoverage coverage) {
			final String name = coverage.getName();
			assertTrue("Class already processed: " + name, classes.add(name));
		}
	}

	@Before
	public void setup() {
		analyzer = new Analyzer(new ExecutionDataStore(),
				new EmptyStructureVisitor());
	}

	@Test
	public void testAnalyzeClass1() throws IOException {
		analyzer.analyzeClass(TargetLoader.getClassData(AnalyzerTest.class),
				"Test");
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeClass2() throws IOException {
		analyzer.analyzeClass(
				TargetLoader.getClassDataAsBytes(AnalyzerTest.class), "Test");
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeClass_Broken() throws IOException {
		final byte[] brokenclass = TargetLoader
				.getClassDataAsBytes(AnalyzerTest.class);
		brokenclass[10] = 0x23;
		try {
			analyzer.analyzeClass(brokenclass, "Broken");
			fail();
		} catch (IOException e) {
			assertEquals("Error while analyzing class Broken.", e.getMessage());
		}
	}

	@Test
	public void testAnalyzeAll_Class() throws IOException {
		final int count = analyzer.analyzeAll(
				TargetLoader.getClassData(AnalyzerTest.class), "Test");
		assertEquals(1, count);
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeAll_Zip() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(buffer);
		zip.putNextEntry(new ZipEntry(
				"org/jacoco/core/analysis/AnalyzerTest.class"));
		zip.write(TargetLoader.getClassDataAsBytes(AnalyzerTest.class));
		zip.finish();
		final int count = analyzer.analyzeAll(
				new ByteArrayInputStream(buffer.toByteArray()), "Test");
		assertEquals(1, count);
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeAll_Pack200() throws IOException {
		final ByteArrayOutputStream zipbuffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(zipbuffer);
		zip.putNextEntry(new ZipEntry(
				"org/jacoco/core/analysis/AnalyzerTest.class"));
		zip.write(TargetLoader.getClassDataAsBytes(AnalyzerTest.class));
		zip.finish();

		final ByteArrayOutputStream pack200buffer = new ByteArrayOutputStream();
		GZIPOutputStream gzipOutput = new GZIPOutputStream(pack200buffer);
		Pack200.newPacker().pack(
				new JarInputStream(new ByteArrayInputStream(
						zipbuffer.toByteArray())), gzipOutput);
		gzipOutput.finish();

		final int count = analyzer.analyzeAll(new ByteArrayInputStream(
				pack200buffer.toByteArray()), "Test");
		assertEquals(1, count);
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeAll_Empty() throws IOException {
		final int count = analyzer.analyzeAll(new ByteArrayInputStream(
				new byte[0]), "Test");
		assertEquals(0, count);
		assertEquals(Collections.emptySet(), classes);
	}

	@Test
	public void testAnalyzeAll_Folder() throws IOException {
		createClassfile("bin1", AnalyzerTest.class);
		final int count = analyzer.analyzeAll(folder.getRoot());
		assertEquals(1, count);
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeAll_Path() throws IOException {
		createClassfile("bin1", Analyzer.class);
		createClassfile("bin2", AnalyzerTest.class);
		String path = "bin1" + File.pathSeparator + "bin2";
		final int count = analyzer.analyzeAll(path, folder.getRoot());
		assertEquals(2, count);
		assertEquals(
				new HashSet<String>(Arrays.asList(
						"org/jacoco/core/analysis/Analyzer",
						"org/jacoco/core/analysis/AnalyzerTest")), classes);
	}

	@Test(expected = IOException.class)
	public void testAnalyzeAll_BrokenZip() throws IOException {
		File file = new File(folder.getRoot(), "broken.zip");
		OutputStream out = new FileOutputStream(file);
		ZipOutputStream zip = new ZipOutputStream(out);
		zip.putNextEntry(new ZipEntry("brokenentry.txt"));
		out.write(0x23); // Unexpected data here
		zip.close();
		analyzer.analyzeAll(file);
	}

	@Test
	public void testAnalyzeAll_BrokenClassFileInZip() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(buffer);
		zip.putNextEntry(new ZipEntry(
				"org/jacoco/core/analysis/AnalyzerTest.class"));
		final byte[] brokenclass = TargetLoader
				.getClassDataAsBytes(AnalyzerTest.class);
		brokenclass[10] = 0x23;
		zip.write(brokenclass);
		zip.finish();

		try {
			analyzer.analyzeAll(new ByteArrayInputStream(buffer.toByteArray()),
					"test.zip");
			fail();
		} catch (IOException e) {
			assertEquals(
					"Error while analyzing class test.zip@org/jacoco/core/analysis/AnalyzerTest.class.",
					e.getMessage());
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

}
