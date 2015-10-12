/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IAnalyzer;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ICoverageFetcherStyle;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

public class DirectReportGeneratorTest {

	@Test
	public void testDefaultConstructor() {
		DirectReportGenerator instance = new DirectReportGenerator();
		assertEquals(new File("target/jacoco.exec"), instance.getDataFile());
	}

	@Test
	public void testSetGetDataFile() {
		DirectReportGenerator instance = new DirectReportGenerator();
		instance.setDataFile(new File("bin/jacoco.exec"));
		assertEquals(new File("bin/jacoco.exec"), instance.getDataFile());
		instance.setDataFile(null);
		assertEquals(new File("target/jacoco.exec"), instance.getDataFile());
	}

	@Test
	public void testAnalyzeExecutionData() throws Exception {
		final List<File> analyzedFiles = new ArrayList<File>();
		DirectReportGenerator instance = new DirectReportGenerator();
		instance.analyzeExecutionData(new IAnalyzer() {

			public void analyzeClass(InputStream input, String name)
					throws IOException {
				throw new UnsupportedOperationException();
			}

			public void analyzeClass(byte[] buffer, String name)
					throws IOException {
				throw new UnsupportedOperationException();
			}

			public void analyzeClass(ClassReader reader) throws IOException {
				throw new UnsupportedOperationException();
			}

			public int analyzeAll(String path, File basedir) throws IOException {
				throw new UnsupportedOperationException();
			}

			public int analyzeAll(File file) throws IOException {
				analyzedFiles.add(file);
				return 1;
			}

			public int analyzeAll(InputStream input, String name)
					throws IOException {
				throw new UnsupportedOperationException();
			}
		});
		assertEquals(instance.getClassesDirectories(), analyzedFiles);
	}

	@Test
	public void testLoadExecutionData() throws Exception {
		final List<File> loadedFiles = new ArrayList<File>();

		DirectReportGenerator instance = new DirectReportGenerator();
		instance.loadExecutionData(new ICoverageFetcherStyle() {

			public void loadExecutionData(File dataFile) throws IOException {
				loadedFiles.add(dataFile);
			}

			public void loadExecutionData(InputStream stream)
					throws IOException {
				throw new UnsupportedOperationException();
			}

			public SessionInfoStore getSessionInfoStore() {
				throw new UnsupportedOperationException();
			}

			public ExecutionDataStore getExecutionDataStore() {
				throw new UnsupportedOperationException();
			}

			public CoverageBuilder newCoverageBuilder() {
				throw new UnsupportedOperationException();
			}

			public IAnalyzer newAnalyzer(CoverageBuilder builder)
					throws IOException {
				throw new UnsupportedOperationException();
			}
		});
		assertEquals(1, loadedFiles.size());
		assertEquals(instance.getDataFile(), loadedFiles.get(0));
	}

	@Test
	public void testCanGenerateReport_success() {
		DirectReportGenerator instance = new DirectReportGenerator();
		assertTrue(instance.canGenerateReport());
	}

	@Test
	public void testCanGenerateReport_noDataFile() {
		DirectReportGenerator instance = new DirectReportGenerator();
		instance.setDataFile(new File("No-Such-File"));
		assertFalse(instance.canGenerateReport());
	}

	@Test
	public void testCanGenerateReport_noClassDir() {
		DirectReportGenerator instance = new DirectReportGenerator();
		instance.setClassesDirectories(new ArrayList<File>());
		assertFalse(instance.canGenerateReport());
	}
}
