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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.Java9Support;
import org.jacoco.core.internal.data.CRC64;
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
		// class IDs are always calculated after downgrade of the version
		final byte[] bytes = Java9Support.downgradeIfRequired(
				TargetLoader.getClassDataAsBytes(AnalyzerTest.class));
		executionData.get(Long.valueOf(CRC64.checksum(bytes)),
				"org/jacoco/core/analysis/AnalyzerTest", 200);
		analyzer.analyzeClass(bytes, "Test");
		assertFalse(classes.get("org/jacoco/core/analysis/AnalyzerTest")
				.isNoMatch());
	}

	@Test
	public void testAnalyzeClassNoIdMatch() throws IOException {
		executionData.get(Long.valueOf(0),
				"org/jacoco/core/analysis/AnalyzerTest", 200);
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
			assertEquals("Error while analyzing Broken.class.", e.getMessage());
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

}
