/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Analyzer}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class AnalyzerTest {

	private Analyzer analyzer;

	private final Set<String> classes = new HashSet<String>();

	private class EmptyStructureVisitor implements ICoverageVisitor {

		public void visitCoverage(ClassCoverage coverage) {
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

}
