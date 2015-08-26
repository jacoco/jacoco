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
package org.jacoco.ebigo.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.ebigo.core.EmpiricalBigOWorkload;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMap;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;
import org.jacoco.ebigo.fit.FitType;
import org.junit.Test;

public class EmpiricalBigOAnalyzerTest {
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_tooFewWorkloads() throws Exception {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore(
				"ATTRIBUTE");
		File classFiles = new File(".");
		new EmpiricalBigOAnalyzer(store, classFiles);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_nullClassDir() throws Exception {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore("KEY");
		addWorkload(store, 10);
		addWorkload(store, 20);
		addWorkload(store, 30);
		addWorkload(store, 40);

		new EmpiricalBigOAnalyzer(store, null);
	}

	@Test(expected = FileNotFoundException.class)
	public void testConstructor_missingClassDir() throws Exception {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore("KEY");
		addWorkload(store, 10);
		addWorkload(store, 20);
		addWorkload(store, 30);
		addWorkload(store, 40);

		File classDir = File.createTempFile("class-", ".d");
		classDir.delete();
		new EmpiricalBigOAnalyzer(store, classDir);
	}

	@Test
	public void testConstructor_good() throws Exception {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore("KEY");
		addWorkload(store, 10);
		addWorkload(store, 20);
		addWorkload(store, 30);
		addWorkload(store, 40);

		File classDir = File.createTempFile("class-", ".d");
		classDir.delete();
		classDir.mkdirs();
		classDir.deleteOnExit();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				classDir);

		assertSame(store, instance.getWorkloadData());
		assertEquals(classDir, instance.getClassFiles());

		classDir.delete();
	}

	@Test
	public void testAnalyzeAll_empty() throws Exception {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore("KEY");
		addWorkload(store, 10);
		addWorkload(store, 20);
		addWorkload(store, 30);
		addWorkload(store, 40);

		File classDir = File.createTempFile("class-", ".d");
		classDir.delete();
		classDir.mkdirs();
		classDir.deleteOnExit();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				classDir);

		EmpiricalBigOBuilder visitor = new EmpiricalBigOBuilder(
				FitType.values(), "KEY");
		instance.analyzeAll(visitor);

		assertEquals("{10={KEY=10}, 20={KEY=20}, 30={KEY=30}, 40={KEY=40}}",
				visitor.getXAxisValues().toString());
		assertTrue(visitor.getClasses().isEmpty());

		classDir.delete();
	}

	@Test
	public void testAnalyzeAll_oneClass() throws Exception {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore("KEY");
		addWorkload(store, 10);
		addWorkload(store, 20);
		addWorkload(store, 30);
		addWorkload(store, 40);

		File classDir = new File("target/classes");
		classDir.delete();
		classDir.mkdirs();
		classDir.deleteOnExit();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				classDir);

		EmpiricalBigOBuilder visitor = new EmpiricalBigOBuilder(
				FitType.values(), "KEY");
		int classCount = instance.analyzeAll(visitor);
		assertTrue(classCount > 0);

		assertEquals("{10={KEY=10}, 20={KEY=20}, 30={KEY=30}, 40={KEY=40}}",
				visitor.getXAxisValues().toString());
		IClassEmpiricalBigO foundClassData = null;
		for (IClassEmpiricalBigO classData : visitor.getClasses()) {
			if (classData.getMatchedCoverageClasses()[0].getName().equals(
					"org/jacoco/ebigo/internal/util/ValidationUtilTest")) {
				foundClassData = classData;
			}
		}
		assertNotNull(foundClassData);

		classDir.delete();
	}

	private void addWorkload(EmpiricalBigOWorkloadStore bigoStore, int value)
			throws IOException {
		WorkloadAttributeMap attributes = WorkloadAttributeMapBuilder.create(
				"KEY", value).build();

		ExecutionDataStore dataStore = new ExecutionDataStore();
		// dataStore.put(makeClassExecutionData(
		// "org/jacoco/ebigo/analysis/EmpiricalBigOAnalyzerTest", value));
		// dataStore.put(makeClassExecutionData(
		// "org/jacoco/ebigo/internal/util/ValidationUtilTest", value));

		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		EmpiricalBigOWorkload workload = new EmpiricalBigOWorkload(attributes,
				dataStore, sessionInfoStore);
		bigoStore.put(attributes, workload);
	}
}