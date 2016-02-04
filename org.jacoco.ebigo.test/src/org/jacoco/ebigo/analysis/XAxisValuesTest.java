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
package org.jacoco.ebigo.analysis;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.ebigo.core.EmpiricalBigOWorkload;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMap;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;
import org.junit.Test;

public class XAxisValuesTest {
	@Test
	public void testEmptyStore() {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore("KEY");

		XAxisValues instance = new XAxisValues(store, "KEY");
		assertEquals(0, instance.size());
		assertEquals(0, instance.getXKeys().length);
		assertEquals(0, instance.getXValues().length);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotARequiredAttribute() {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore("KEY");

		new XAxisValues(store, "OTHER");
	}

	private EmpiricalBigOWorkload makeWorkload(WorkloadAttributeMap attributes) {
		ExecutionDataStore executionDataStore = new ExecutionDataStore();
		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		EmpiricalBigOWorkload workload = new EmpiricalBigOWorkload(attributes,
				executionDataStore, sessionInfoStore);
		return workload;
	}

	@Test
	public void testEmptyMixedStore() {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore("KEY");
		WorkloadAttributeMap attributes1 = WorkloadAttributeMapBuilder
				.create("KEY", 10).add("EXTRA", 3).build();
		WorkloadAttributeMap attributes2 = WorkloadAttributeMapBuilder
				.create("KEY", 20).add("EXTRA", 2).build();
		WorkloadAttributeMap attributes3 = WorkloadAttributeMapBuilder
				.create("KEY", 30).add("EXTRA", 1).build();
		store.put(makeWorkload(attributes1));
		store.put(makeWorkload(attributes2));
		store.put(makeWorkload(attributes3));

		XAxisValues instance = new XAxisValues(store, "KEY");
		assertEquals(3, instance.size());
		assertEquals(3, instance.getXKeys().length);
		assertEquals(3, instance.getXValues().length);
		assertEquals(
				"{10={EXTRA=3, KEY=10}, 20={EXTRA=2, KEY=20}, 30={EXTRA=1, KEY=30}}",
				instance.toString());
	}
}