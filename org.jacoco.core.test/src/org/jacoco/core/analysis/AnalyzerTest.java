/*******************************************************************************
 * Copyright (c) Copyright (c) Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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
		analyzer.analyzeClass(TargetLoader.getClassData(AnalyzerTest.class));
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeClass2() throws IOException {
		analyzer.analyzeClass(TargetLoader
				.getClassDataAsBytes(AnalyzerTest.class));
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeArchive() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(buffer);
		zip.putNextEntry(new ZipEntry(
				"org/jacoco/core/analysis/AnalyzerTest.class"));
		zip.write(TargetLoader.getClassDataAsBytes(AnalyzerTest.class));
		zip.finish();
		final int count = analyzer.analyzeArchive(new ByteArrayInputStream(
				buffer.toByteArray()));
		assertEquals(1, count);
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeAll1() throws IOException {
		final int count = analyzer.analyzeAll(TargetLoader
				.getClassData(AnalyzerTest.class));
		assertEquals(1, count);
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeAll2() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(buffer);
		zip.putNextEntry(new ZipEntry(
				"org/jacoco/core/analysis/AnalyzerTest.class"));
		zip.write(TargetLoader.getClassDataAsBytes(AnalyzerTest.class));
		zip.finish();
		final int count = analyzer.analyzeAll(new ByteArrayInputStream(buffer
				.toByteArray()));
		assertEquals(1, count);
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeAll3() throws IOException {
		final int count = analyzer.analyzeAll(new ByteArrayInputStream(
				new byte[0]));
		assertEquals(0, count);
		assertEquals(Collections.emptySet(), classes);
	}

	@Test
	public void testAnalyzeAll4() throws IOException {
		createClassfile("bin1", AnalyzerTest.class);
		final int count = analyzer.analyzeAll(folder.getRoot());
		assertEquals(1, count);
		assertEquals(
				Collections.singleton("org/jacoco/core/analysis/AnalyzerTest"),
				classes);
	}

	@Test
	public void testAnalyzeAll5() throws IOException {
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
	public void testAnalyzeAll6() throws IOException {
		File file = new File(folder.getRoot(), "broken.zip");
		OutputStream out = new FileOutputStream(file);
		ZipOutputStream zip = new ZipOutputStream(out);
		zip.putNextEntry(new ZipEntry("brokenentry.txt"));
		out.write(0x23); // Unexpected data here
		zip.close();
		analyzer.analyzeAll(file);
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
