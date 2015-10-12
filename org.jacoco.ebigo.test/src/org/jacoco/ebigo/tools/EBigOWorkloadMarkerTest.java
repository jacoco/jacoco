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
package org.jacoco.ebigo.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.ebigo.core.EmpiricalBigOWorkload;
import org.jacoco.ebigo.core.IEBigOConnection;
import org.jacoco.ebigo.core.WorkloadAttributeMap;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;
import org.junit.Test;

public final class EBigOWorkloadMarkerTest {

	@Test
	public void testGetInstance() {
		EBigOWorkloadMarker instance1 = EBigOWorkloadMarker.getInstance();
		EBigOWorkloadMarker instance2 = EBigOWorkloadMarker.getInstance();
		try {
			assertEquals("localhost", instance1.getHostname());
			assertEquals(AgentOptions.DEFAULT_PORT, instance1.getPort());
			assertEquals("target", instance1.getProjectBuildDir());
			assertEquals("jacoco.exec", instance1.getDestfile());
			assertEquals(WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE,
					instance1.getEbigoAttribute());
			assertSame(instance1, instance2);
		} finally {
			instance1.close();
			instance2.close();
		}
	}

	@Test
	public void testGetSetHostname() {
		EBigOWorkloadMarker instance = EBigOWorkloadMarker.getInstance();
		try {
			instance.setHostname("another-host");
			assertEquals("another-host", instance.getHostname());
		} finally {
			instance.close();
		}
	}

	@Test
	public void testGetSetPort() {
		EBigOWorkloadMarker instance = EBigOWorkloadMarker.getInstance();
		try {
			instance.setPort(12345);
			assertEquals(12345, instance.getPort());
		} finally {
			instance.close();
		}
	}

	@Test
	public void testGetSetProjectBuildDir() {
		EBigOWorkloadMarker instance = EBigOWorkloadMarker.getInstance();
		try {
			instance.setProjectBuildDir("another-directory");
			assertEquals("another-directory", instance.getProjectBuildDir());
		} finally {
			instance.close();
		}
	}

	@Test
	public void testGetSetDestfile() {
		EBigOWorkloadMarker instance = EBigOWorkloadMarker.getInstance();
		try {
			instance.setDestfile("another-file");
			assertEquals("another-file", instance.getDestfile());
		} finally {
			instance.close();
		}
	}

	@Test
	public void testGetSetEbigoAttribute() {
		EBigOWorkloadMarker instance = EBigOWorkloadMarker.getInstance();
		try {
			instance.setEbigoAttribute("ANOTHER_ATTRIBUTE");
			assertEquals("ANOTHER_ATTRIBUTE", instance.getEbigoAttribute());
		} finally {
			instance.close();
		}
	}

	private static class TestConnection implements IEBigOConnection {
		private int resetCount = 0;

		public EmpiricalBigOWorkload fetchWorkloadCoverage(
				WorkloadAttributeMap attributeMap) throws IOException {
			return new EmpiricalBigOWorkload(attributeMap,
					new ExecutionDataStore(), new SessionInfoStore());
		}

		public EmpiricalBigOWorkload fetchWorkloadCoverage(int attributeValue)
				throws IOException {
			final WorkloadAttributeMap attributeMap = WorkloadAttributeMapBuilder
					.create(attributeValue).build();
			return fetchWorkloadCoverage(attributeMap);
		}

		public EmpiricalBigOWorkload fetchWorkloadCoverage(
				String attributeName, int attributeValue) throws IOException {
			final WorkloadAttributeMap attributeMap = WorkloadAttributeMapBuilder
					.create(attributeName, attributeValue).build();
			return fetchWorkloadCoverage(attributeMap);
		}

		public void resetCoverage() throws IOException {
			resetCount++;
		}

		public int getResetCount() {
			return resetCount;
		}

		public void close() throws IOException {
		}

	}

	private static void injectConnection(EBigOWorkloadMarker marker,
			TestConnection connection) throws Exception {
		try {
			Field field = marker.getClass().getDeclaredField("connection");
			field.setAccessible(true);
			field.set(marker, connection);
		} catch (Exception e) {
			throw new RuntimeException("Failed to inject connection", e);
		}
	}

	@Test
	public void testBeginWorkload() throws Exception {
		EBigOWorkloadMarker instance = EBigOWorkloadMarker.getInstance();
		try {
			TestConnection connection = new TestConnection();
			injectConnection(instance, connection);

			instance.beginWorkload();

			assertEquals(1, connection.getResetCount());
		} finally {
			instance.close();
		}
	}

	@Test
	public void testEndWorkload_directoryIsFile_resultWritten()
			throws Exception {
		EBigOWorkloadMarker instance = EBigOWorkloadMarker.getInstance();
		File tempDir = File.createTempFile("jacoco", ".exec");
		assertTrue(tempDir.exists());
		try {
			TestConnection connection = new TestConnection();
			injectConnection(instance, connection);

			instance.setProjectBuildDir(tempDir.getParentFile()
					.getAbsolutePath());
			instance.setDestfile(tempDir.getName());
			instance.endWorkload(123);

			assertTrue(tempDir.exists());
			assertEquals(0, connection.getResetCount());
		} finally {
			instance.close();
			tempDir.delete();
		}
	}

	@Test
	public void testEndWorkload_directoryDoesntExists_resultWritten()
			throws Exception {
		EBigOWorkloadMarker instance = EBigOWorkloadMarker.getInstance();
		File tempDir = File.createTempFile("ebigo", ".dir");
		tempDir.delete();
		try {
			TestConnection connection = new TestConnection();
			injectConnection(instance, connection);

			instance.setProjectBuildDir(tempDir.getAbsolutePath());
			instance.endWorkload(123);

			assertTrue(tempDir.exists());
			assertEquals(0, connection.getResetCount());
		} finally {
			instance.close();
			tempDir.delete();
		}
	}

	@Test
	public void testEndWorkload_directoryExists_resultWritten()
			throws Exception {
		EBigOWorkloadMarker instance = EBigOWorkloadMarker.getInstance();
		File tempDir = File.createTempFile("ebigo", ".dir");
		tempDir.delete();
		tempDir.mkdirs();
		try {
			TestConnection connection = new TestConnection();
			injectConnection(instance, connection);

			instance.setProjectBuildDir(tempDir.getAbsolutePath());
			instance.endWorkload(123);

			assertTrue(tempDir.getParentFile().exists());
			assertTrue(tempDir.exists());
			assertEquals(0, connection.getResetCount());
		} finally {
			instance.close();
			tempDir.delete();
		}
	}
}