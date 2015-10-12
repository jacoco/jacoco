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
package org.jacoco.core.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IAnalyzer;
import org.jacoco.core.data.ExecutionDataStore;
import org.junit.Test;

public class DefaultCoverageFetcherStyleTest {

	private static final File DATA_FILE = new File(
			DefaultCoverageFetcherStyle.class.getResource("sample.exec")
					.getPath());

	@Test
	public void testConstructor() {
		DefaultCoverageFetcherStyle instance = new DefaultCoverageFetcherStyle();
		assertEquals(0, instance.getExecutionDataStore().getContents().size());
		assertEquals(0, instance.getSessionInfoStore().getInfos().size());
	}

	@Test(expected = FileNotFoundException.class)
	public void testLoadExecutionData_badExecData() throws IOException {
		File badDataFile = File.createTempFile("jacoco", ".exec");
		badDataFile.delete();
		DefaultCoverageFetcherStyle instance = new DefaultCoverageFetcherStyle();
		instance.loadExecutionData(badDataFile);
	}

	@Test
	public void testLoadExecutionData() throws IOException {
		DefaultCoverageFetcherStyle instance = new DefaultCoverageFetcherStyle();
		instance.loadExecutionData(DATA_FILE);
		ExecutionDataStore executionDataStore = instance
				.getExecutionDataStore();
		assertEquals(1, executionDataStore.getContents().size());
	}

	@Test
	public void testNewCoverageBuilder() throws IOException {
		DefaultCoverageFetcherStyle instance = new DefaultCoverageFetcherStyle();
		CoverageBuilder coverageBuilder = instance.newCoverageBuilder();
		assertEquals(CoverageBuilder.class, coverageBuilder.getClass());
	}

	@Test
	public void testNewAnalyzer() {
		DefaultCoverageFetcherStyle instance = new DefaultCoverageFetcherStyle();
		CoverageBuilder coverageBuilder = instance.newCoverageBuilder();
		IAnalyzer analyzer = instance.newAnalyzer(coverageBuilder);
		assertEquals(Analyzer.class, analyzer.getClass());
	}
}