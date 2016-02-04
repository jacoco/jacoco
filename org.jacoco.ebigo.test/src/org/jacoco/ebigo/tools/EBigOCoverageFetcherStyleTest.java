/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.ebigo.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IAnalyzer;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.ebigo.analysis.EmpiricalBigOAnalyzer;
import org.jacoco.ebigo.analysis.EmpiricalBigOBuilder;
import org.jacoco.ebigo.core.EmpiricalBigOWorkload;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMap;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;
import org.junit.Assert;
import org.junit.Test;

public class EBigOCoverageFetcherStyleTest {

	private static final String EBIGO_ATTRIBUTE = "DEFAULT";
	private static final File DATA_FILE = new File(
			EBigOCoverageFetcherStyle.class.getResource("sample.exec")
					.getPath()).getParentFile();

	@Test
	public void testConstructor() {
		EBigOCoverageFetcherStyle instance = new EBigOCoverageFetcherStyle(
				EBIGO_ATTRIBUTE);
		assertEquals(0, instance.getExecutionDataStore().getContents().size());
		assertEquals(0, instance.getSessionInfoStore().getInfos().size());
	}

	@Test
	public void testLoadExecutionData_badExecData() throws IOException {
		File badDataDir = File.createTempFile("jacoco", ".exec");
		badDataDir.delete();
		badDataDir.mkdirs();
		badDataDir.deleteOnExit();
		EBigOCoverageFetcherStyle instance = new EBigOCoverageFetcherStyle(
				EBIGO_ATTRIBUTE);
		instance.loadExecutionData(badDataDir);
		Assert.assertEquals(0, instance.getExecutionDataStore().getContents()
				.size());
		Assert.assertEquals(0, instance.getSessionInfoStore().getInfos().size());
		badDataDir.delete();
	}

	@Test
	public void testLoadExecutionData() throws IOException {
		EBigOCoverageFetcherStyle instance = new EBigOCoverageFetcherStyle(
				EBIGO_ATTRIBUTE);
		instance.loadExecutionData(DATA_FILE);
		Assert.assertNotNull(instance.getExecutionDataStore());
		Assert.assertNotNull(instance.getSessionInfoStore());
	}

	@Test
	public void testCreateCoverage() throws IOException {
		EBigOCoverageFetcherStyle instance = newTestFetcher();
		instance.loadExecutionData(DATA_FILE);
		CoverageBuilder coverageBuilder = instance.newCoverageBuilder();
		assertEquals(EmpiricalBigOBuilder.class, coverageBuilder.getClass());
	}

	@Test
	public void testCreateAnalyzer() throws IOException {
		EBigOCoverageFetcherStyle instance = newTestFetcher();
		instance.loadExecutionData(DATA_FILE);
		CoverageBuilder coverageBuilder = instance.newCoverageBuilder();
		IAnalyzer analyzer = instance.newAnalyzer(coverageBuilder);
		assertEquals(EmpiricalBigOAnalyzer.class, analyzer.getClass());
	}

	private EBigOCoverageFetcherStyle newTestFetcher() {
		return new EBigOCoverageFetcherStyle(EBIGO_ATTRIBUTE) {
			@Override
			public void loadExecutionData(File dataFile) throws IOException {
				super.loadExecutionData(dataFile);
				final EmpiricalBigOWorkloadStore workloadStore = loader
						.getWorkloadstore();
				final WorkloadAttributeMap key = workloadStore.keySet()
						.iterator().next();
				final String defaultAttribute = workloadStore
						.getDefaultAttribute();
				ExecutionDataStore executionDataStore = workloadStore.get(key)
						.getExecutionDataStore();
				SessionInfoStore sessionInfoStore = workloadStore.get(key)
						.getSessionInfo();
				int nextValue = key.get(defaultAttribute).intValue() + 1;
				for (int i = workloadStore.size(); i <= 5; i++) {
					EmpiricalBigOWorkload workload = new EmpiricalBigOWorkload(
							WorkloadAttributeMapBuilder.create(nextValue++)
									.build(), executionDataStore,
							sessionInfoStore);
					workloadStore.put(workload);
				}
				return;
			}
		};
	}
}