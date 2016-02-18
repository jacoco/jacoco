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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.internal.instr.ProbeArrayService;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.junit.Test;

public class EmpiricalBigOWorkloadTest {
	@Test
	public void testConstructor() {
		WorkloadAttributeMap attributeMap = WorkloadAttributeMapBuilder.create(
				"KEY", 10).build();
		ExecutionDataStore executionDataStore = new ExecutionDataStore();
		SessionInfoStore sessionInfoStore = new SessionInfoStore();

		EmpiricalBigOWorkload instance = new EmpiricalBigOWorkload(
				attributeMap, executionDataStore, sessionInfoStore);

		assertSame(attributeMap, instance.getattributeMap());
		assertSame(executionDataStore, instance.getExecutionDataStore());
		assertSame(sessionInfoStore, instance.getSessionInfo());
	}

	@Test
	public void testCanReadWhatWrote() throws Exception {
		WorkloadAttributeMap attributeMap = WorkloadAttributeMapBuilder.create(
				"KEY", 10).build();
		ExecutionDataStore executionDataStore = new ExecutionDataStore();
		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		EmpiricalBigOWorkload instance = new EmpiricalBigOWorkload(
				attributeMap, executionDataStore, sessionInfoStore);
		File resultsDir = File.createTempFile("ebigoResults-", ".d");
		resultsDir.delete();
		resultsDir.mkdirs();
		resultsDir.deleteOnExit();

		instance.write(resultsDir, "writeThenReadTest");
		EmpiricalBigOWorkload result = EmpiricalBigOWorkload.read(resultsDir,
				"writeThenReadTest");
		resultsDir.delete();

		assertEquals(result.getattributeMap(), instance.getattributeMap());
		assertEquals(result.getExecutionDataStore().getContents().size(),
				instance.getExecutionDataStore().getContents().size());
		assertEquals(result.getSessionInfo().getInfos().size(), instance
				.getSessionInfo().getInfos().size());
	}

	@Test
	public void testStaticReadRemote() throws Exception {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] {});
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		WorkloadAttributeMap attributeMap = WorkloadAttributeMapBuilder.create(
				"KEY", 10).build();
		EmpiricalBigOWorkload result = EmpiricalBigOWorkload.readRemote(
				attributeMap, new RemoteControlWriter(baos),
				new RemoteControlReader(bais));

		assertArrayEquals(new byte[] { 1, -64, -64, 16, 9, 64, 1, 1 },
				baos.toByteArray());
		assertEquals(0, result.getExecutionDataStore().getContents().size());
	}

}