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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.ebigo.core.EmpiricalBigOWorkload;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMap;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;
import org.jacoco.ebigo.internal.util.ValidationUtils;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

public class EmpiricalBigOAnalyzerTest {
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_nullStore() throws Exception {
		EmpiricalBigOWorkloadStore store = null;
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		new EmpiricalBigOAnalyzer(store, builder);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_nullBuilder() throws Exception {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore();
		EmpiricalBigOBuilder builder = null;
		new EmpiricalBigOAnalyzer(store, builder);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_tooFewWorkloads() throws Exception {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		new EmpiricalBigOAnalyzer(store, builder);
	}

	@Test
	public void testConstructor_good() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		assertSame(store, instance.getWorkloadData());
		assertEquals(0, builder.getClasses().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullClassDir() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		instance.analyzeAll(null);
	}

	@Test(expected = FileNotFoundException.class)
	public void testConstructor_missingClassDir() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		File classDir = File.createTempFile("class-", ".d");
		classDir.delete();

		instance.analyzeAll(classDir);
	}

	@Test
	public void testAnalyzeClass_inputStream() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		InputStream classStream = getClassData(ValidationUtils.class);
		instance.analyzeClass(classStream, getClassName(ValidationUtils.class));

		assertEquals(
				"{10={DEFAULT=10}, 20={DEFAULT=20}, 30={DEFAULT=30}, 40={DEFAULT=40}}",
				builder.getXAxisValues().toString());
		final Collection<IClassCoverage> ccs = builder.getClasses();
		assertEquals(1, ccs.size());
		assertEquals(getClassName(ValidationUtils.class), ccs.iterator().next()
				.getName());
	}

	@Test
	public void testAnalyzeClass_reader() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		ClassReader reader = new ClassReader(
				getClassData(ValidationUtils.class));
		instance.analyzeClass(reader);

		assertEquals(
				"{10={DEFAULT=10}, 20={DEFAULT=20}, 30={DEFAULT=30}, 40={DEFAULT=40}}",
				builder.getXAxisValues().toString());
		final Collection<IClassCoverage> ccs = builder.getClasses();
		assertEquals(1, ccs.size());
		assertEquals(getClassName(ValidationUtils.class), ccs.iterator().next()
				.getName());
	}

	@Test
	public void testAnalyzeClass_byteArray() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		byte[] byteArray = getClassDataAsBytes(ValidationUtils.class);
		instance.analyzeClass(byteArray, getClassName(ValidationUtils.class));

		assertEquals(
				"{10={DEFAULT=10}, 20={DEFAULT=20}, 30={DEFAULT=30}, 40={DEFAULT=40}}",
				builder.getXAxisValues().toString());
		final Collection<IClassCoverage> ccs = builder.getClasses();
		assertEquals(1, ccs.size());
		assertEquals(getClassName(ValidationUtils.class), ccs.iterator().next()
				.getName());
	}

	@Test
	public void testAnalyzeAll_empty() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		File classDir = File.createTempFile("class-", ".d");
		classDir.delete();
		classDir.mkdirs();
		classDir.deleteOnExit();

		int result = instance.analyzeAll(classDir);

		classDir.delete();

		assertEquals(0, result);
		assertEquals(
				"{10={DEFAULT=10}, 20={DEFAULT=20}, 30={DEFAULT=30}, 40={DEFAULT=40}}",
				builder.getXAxisValues().toString());
		assertTrue(builder.getClasses().isEmpty());
	}

	@Test
	public void testAnalyzeAll_File_oneClass() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		File classDir = new File("target/classes");
		int classCount = instance.analyzeAll(classDir);

		assertTrue(classCount > 0);

		assertEquals(
				"{10={DEFAULT=10}, 20={DEFAULT=20}, 30={DEFAULT=30}, 40={DEFAULT=40}}",
				builder.getXAxisValues().toString());
		IClassCoverage foundClassData = null;
		for (final IClassCoverage classData : builder.getClasses()) {
			if (classData.getName().equals(
					"org/jacoco/ebigo/internal/util/ValidationUtilTest")) {
				foundClassData = classData;
			}
		}
		assertNotNull(foundClassData);
	}

	@Test
	public void testAnalyzeAll_path_oneClass() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		int classCount = instance.analyzeAll("classes", new File("target"));

		assertTrue(classCount > 0);

		assertEquals(
				"{10={DEFAULT=10}, 20={DEFAULT=20}, 30={DEFAULT=30}, 40={DEFAULT=40}}",
				builder.getXAxisValues().toString());
		IClassCoverage foundClassData = null;
		for (final IClassCoverage classData : builder.getClasses()) {
			if (classData.getName().equals(
					"org/jacoco/ebigo/internal/util/ValidationUtilTest")) {
				foundClassData = classData;
			}
		}
		assertNotNull(foundClassData);
	}

	@Test
	public void testAnalyzeAll_inputStream_oneClass() throws Exception {
		EmpiricalBigOWorkloadStore store = makeWorkstore();
		EmpiricalBigOBuilder builder = new EmpiricalBigOBuilder();
		EmpiricalBigOAnalyzer instance = new EmpiricalBigOAnalyzer(store,
				builder);

		InputStream classStream = getClassData(ValidationUtils.class);
		instance.analyzeAll(classStream, getClassName(ValidationUtils.class));

		assertEquals(
				"{10={DEFAULT=10}, 20={DEFAULT=20}, 30={DEFAULT=30}, 40={DEFAULT=40}}",
				builder.getXAxisValues().toString());
		final Collection<IClassCoverage> ccs = builder.getClasses();
		assertEquals(1, ccs.size());
		assertEquals(getClassName(ValidationUtils.class), ccs.iterator().next()
				.getName());
	}

	private EmpiricalBigOWorkloadStore makeWorkstore() throws IOException {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore();
		addWorkload(store, 10);
		addWorkload(store, 20);
		addWorkload(store, 30);
		addWorkload(store, 40);
		return store;
	}

	private void addWorkload(EmpiricalBigOWorkloadStore bigoStore, int value)
			throws IOException {
		WorkloadAttributeMap attributes = WorkloadAttributeMapBuilder.create(
				value).build();

		ExecutionDataStore executionDataStore = new ExecutionDataStore();

		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		EmpiricalBigOWorkload workload = new EmpiricalBigOWorkload(attributes,
				executionDataStore, sessionInfoStore);
		bigoStore.put(workload);
	}

	public static byte[] getClassDataAsBytes(Class<?> clazz) throws IOException {
		InputStream in = getClassData(clazz);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[0x100];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		in.close();
		return out.toByteArray();
	}

	private static InputStream getClassData(Class<?> clazz) throws IOException {
		final String resource = "/" + getClassName(clazz) + ".class";
		return new ByteArrayInputStream(
				toByteArray(clazz.getResourceAsStream(resource)));
	}

	private static String getClassName(Class<?> clazz) {
		return clazz.getName().replace('.', '/');
	}

	private static byte[] toByteArray(final InputStream input)
			throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		return output.toByteArray();
	}

}