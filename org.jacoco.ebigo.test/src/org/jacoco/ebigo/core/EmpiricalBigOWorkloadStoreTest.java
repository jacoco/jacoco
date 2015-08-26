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
package org.jacoco.ebigo.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.junit.Test;

public class EmpiricalBigOWorkloadStoreTest {
	@Test(expected = IllegalArgumentException.class)
	public void constructorNoAttributes() {
		new EmpiricalBigOWorkloadStore(null);
	}

	@Test
	public void constructorOneAttributes1() {
		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY");
		assertEquals("[KEY]", instance.getRequiredAttributes().toString());
	}

	@Test
	public void constructorOneAttributes2() {
		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY", (String[]) null);
		assertEquals("[KEY]", instance.getRequiredAttributes().toString());
		assertEquals(0, instance.size());
		assertEquals(0, instance.getXAxisValues("KEY").getXKeys().length);
	}

	@Test
	public void constructorTwoAttributes() {
		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY1", "KEY2");
		assertEquals(instance.getRequiredAttributes().toString(),
				"[KEY1, KEY2]");
		assertEquals(0, instance.size());
		assertEquals(0, instance.getXAxisValues("KEY1").getXKeys().length);
	}

	@Test
	public void testGetWhatPut() {
		WorkloadAttributeMap attributes = WorkloadAttributeMapBuilder.create(
				"KEY", 10).build();
		ExecutionDataStore executionDataStore = new ExecutionDataStore();
		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		EmpiricalBigOWorkload workload = new EmpiricalBigOWorkload(attributes,
				executionDataStore, sessionInfoStore);

		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY");

		instance.put(attributes, workload);
		EmpiricalBigOWorkload result = instance.get(attributes);

		assertEquals(attributes, result.getattributeMap());
		assertSame(executionDataStore, result.getExecutionDataStore());
		assertSame(sessionInfoStore, result.getSessionInfo());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutWithMissingAttributes() {
		WorkloadAttributeMap attributes = WorkloadAttributeMapBuilder.create()
				.build();
		ExecutionDataStore executionDataStore = new ExecutionDataStore();
		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		EmpiricalBigOWorkload workload = new EmpiricalBigOWorkload(attributes,
				executionDataStore, sessionInfoStore);

		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY");

		instance.put(attributes, workload);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutWithDuplicateAttributes() {
		WorkloadAttributeMap attributes = WorkloadAttributeMapBuilder.create(
				"KEY", 10).build();
		ExecutionDataStore executionDataStore = new ExecutionDataStore();
		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		EmpiricalBigOWorkload workload = new EmpiricalBigOWorkload(attributes,
				executionDataStore, sessionInfoStore);

		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY");

		instance.put(attributes, workload);
		instance.put(attributes, workload);
	}

}