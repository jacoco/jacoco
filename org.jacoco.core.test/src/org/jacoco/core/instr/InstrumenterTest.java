/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.analysis.AnalyzerTest;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Instrumenter}.
 */
public class InstrumenterTest {

	// no serialVersionUID to enforce calculation
	@SuppressWarnings("serial")
	public static class SerializationTarget implements Serializable {

		private final String text;

		private final int nr;

		public SerializationTarget(final String text, final int nr) {
			this.text = text;
			this.nr = nr;
		}

		@Override
		public String toString() {
			return text + nr;
		}

	}

	private SystemPropertiesRuntime runtime;

	private Instrumenter instrumenter;

	@Before
	public void setup() throws Exception {
		runtime = new SystemPropertiesRuntime();
		instrumenter = new Instrumenter(runtime);
		runtime.startup(new RuntimeData());
	}

	@After
	public void teardown() {
		runtime.shutdown();
	}

	@Test
	public void testInstrumentClass() throws Exception {
		byte[] bytes = instrumenter.instrument(
				TargetLoader.getClassDataAsBytes(InstrumenterTest.class),
				"Test");
		TargetLoader loader = new TargetLoader();
		Class<?> clazz = loader.add(InstrumenterTest.class, bytes);
		assertEquals("org.jacoco.core.instr.InstrumenterTest", clazz.getName());
	}

	/**
	 * Triggers exception in {@link Instrumenter#instrument(byte[], String)}.
	 */
	@Test
	public void testInstrumentBrokenClass1() throws IOException {
		final byte[] brokenclass = TargetLoader
				.getClassDataAsBytes(AnalyzerTest.class);
		brokenclass[10] = 0x23;
		try {
			instrumenter.instrument(brokenclass, "Broken.class");
			fail();
		} catch (IOException e) {
			assertEquals("Error while instrumenting Broken.class.",
					e.getMessage());
		}
	}

	private static class BrokenInputStream extends InputStream {

		@Override
		public int read() throws IOException {
			throw new IOException();
		}
	}

	/**
	 * Triggers exception in
	 * {@link Instrumenter#instrument(InputStream, String)}.
	 */
	@Test
	public void testInstrumentBrokenStream() {
		try {
			instrumenter.instrument(new BrokenInputStream(), "BrokenStream");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting BrokenStream.",
					e.getMessage());
		}
	}

	/**
	 * Triggers exception in
	 * {@link Instrumenter#instrument(InputStream, OutputStream, String)}.
	 */
	@Test
	public void testInstrumentBrokenStream2() {
		try {
			instrumenter.instrument(new BrokenInputStream(),
					new ByteArrayOutputStream(), "BrokenStream");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting BrokenStream.",
					e.getMessage());
		}
	}

	@Test
	public void testSerialization() throws Exception {
		// Create instrumented instance:
		byte[] bytes = instrumenter.instrument(
				TargetLoader.getClassData(SerializationTarget.class), "Test");
		TargetLoader loader = new TargetLoader();
		Object obj1 = loader.add(SerializationTarget.class, bytes)
				.getConstructor(String.class, Integer.TYPE)
				.newInstance("Hello", Integer.valueOf(42));

		// Serialize instrumented instance:
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		new ObjectOutputStream(buffer).writeObject(obj1);

		// Deserialize with original class definition:
		Object obj2 = new ObjectInputStream(new ByteArrayInputStream(
				buffer.toByteArray())).readObject();
		assertEquals("Hello42", obj2.toString());
	}
}