/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.internal.instr.IProbeArray;
import org.jacoco.core.internal.instr.ProbeIntArray;
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
		assertEquals("KEY", instance.getDefaultAttribute());
	}

	@Test
	public void constructorOneAttributes2() {
		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY", (String[]) null);
		assertEquals("[KEY]", instance.getRequiredAttributes().toString());
		assertEquals(0, instance.size());
		assertEquals(0, instance.getXAxisValues("KEY").getXKeys().length);
		assertEquals("KEY", instance.getDefaultAttribute());
	}

	@Test
	public void constructorTwoAttributes() {
		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY1", "KEY2");
		assertEquals(instance.getRequiredAttributes().toString(),
				"[KEY1, KEY2]");
		assertEquals(0, instance.size());
		assertEquals(0, instance.getXAxisValues("KEY1").getXKeys().length);
		assertEquals("KEY1", instance.getDefaultAttribute());
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

		instance.put(workload);
		EmpiricalBigOWorkload result = instance.get(attributes);

		assertEquals(attributes, result.getattributeMap());
		assertSame(executionDataStore, result.getExecutionDataStore());
		assertSame(sessionInfoStore, result.getSessionInfo());
		assertEquals("KEY", instance.getDefaultAttribute());
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

		instance.put(workload);
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

		instance.put(workload);
		instance.put(workload);
	}

	@Test
	public void testGetMergedExecutionDataStore() {
		// create 2 work loads each with one store for same class
		WorkloadAttributeMap attributes1 = WorkloadAttributeMapBuilder.create(
				"KEY", 1).build();
		ExecutionDataStore dataStore1 = new ExecutionDataStore();
		dataStore1.put(new ExecutionData(1L, "MyClass",
				createIntProbe(new int[] { 1, 2, 3 })));
		SessionInfoStore infoStore1 = new SessionInfoStore();
		EmpiricalBigOWorkload workload1 = new EmpiricalBigOWorkload(
				attributes1, dataStore1, infoStore1);

		WorkloadAttributeMap attributes2 = WorkloadAttributeMapBuilder.create(
				"KEY", 2).build();
		ExecutionDataStore dataStore2 = new ExecutionDataStore();
		dataStore2.put(new ExecutionData(1L, "MyClass",
				createIntProbe(new int[] { 3, 2, 1 })));
		SessionInfoStore infoStore2 = new SessionInfoStore();
		EmpiricalBigOWorkload workload2 = new EmpiricalBigOWorkload(
				attributes2, dataStore2, infoStore2);

		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY");
		instance.put(workload1);
		instance.put(workload2);

		{ // store are merged
			IProbeArray<?> expectedProbe = createIntProbe(new int[] { 4, 4, 4 });
			IProbeArray<?> actualProbe = (IProbeArray<?>) instance
					.getMergedExecutionDataStore().get(1L).getProbes();
			assertEquals(expectedProbe, actualProbe);
		}

		{ // original not changed
			IProbeArray<?> expectedProbe1 = createIntProbe(new int[] { 1, 2, 3 });
			IProbeArray<?> expectedProbe2 = createIntProbe(new int[] { 3, 2, 1 });
			assertEquals(expectedProbe1, dataStore1.get(1L).getProbes());
			assertEquals(expectedProbe2, dataStore2.get(1L).getProbes());
		}
	}

	@Test
	public void testGetMergedSessionInfoStore() {
		// create 2 work loads each with one store for same class
		WorkloadAttributeMap attributes1 = WorkloadAttributeMapBuilder.create(
				"KEY", 1).build();
		ExecutionDataStore dataStore1 = new ExecutionDataStore();
		SessionInfoStore infoStore1 = new SessionInfoStore();
		infoStore1.visitSessionInfo(new SessionInfo("1", 1, 2));
		EmpiricalBigOWorkload workload1 = new EmpiricalBigOWorkload(
				attributes1, dataStore1, infoStore1);

		WorkloadAttributeMap attributes2 = WorkloadAttributeMapBuilder.create(
				"KEY", 2).build();
		ExecutionDataStore dataStore2 = new ExecutionDataStore();
		SessionInfoStore infoStore2 = new SessionInfoStore();
		infoStore2.visitSessionInfo(new SessionInfo("1", 3, 4));
		EmpiricalBigOWorkload workload2 = new EmpiricalBigOWorkload(
				attributes2, dataStore2, infoStore2);

		EmpiricalBigOWorkloadStore instance = new EmpiricalBigOWorkloadStore(
				"KEY");
		instance.put(workload1);
		instance.put(workload2);

		{ // store are merged
			SessionInfoStore merged = instance.getMergedSessionInfoStore();
			assertEquals(2, merged.getInfos().size());
		}
	}

	private ProbeIntArray createIntProbe(int[] probeValues) {
		ProbeIntArray probes = (ProbeIntArray) ProbeMode.count
				.getProbeZeroInstance().newProbeArray(probeValues.length);
		for (int i = 0; i < probeValues.length; i++) {
			for (int j = probeValues[i]; j > 0; j--) {
				probes.increment(i);
			}
		}
		return probes;
	}
}