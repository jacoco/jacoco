/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
	public void testSerialization() throws Exception {
		// Create instrumented instance:
		byte[] bytes = instrumenter.instrument(TargetLoader
				.getClassData(SerializationTarget.class));
		TargetLoader loader = new TargetLoader(SerializationTarget.class, bytes);
		Object obj1 = loader.getTargetClass()
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

	@Test
	public void testInstrumentAll_Class() throws IOException {
		InputStream in = TargetLoader.getClassData(getClass());
		OutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(in, out);

		assertEquals(1, count);
	}

	@Test
	public void testInstrumentAll_Zip() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(buffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		zipout.write(TargetLoader.getClassDataAsBytes(getClass()));
		zipout.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(
				new ByteArrayInputStream(buffer.toByteArray()), out);

		assertEquals(1, count);
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				out.toByteArray()));
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	@Test
	public void testInstrumentAll_Pack200() throws IOException {
		ByteArrayOutputStream jarbuffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(jarbuffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		zipout.write(TargetLoader.getClassDataAsBytes(getClass()));
		zipout.finish();

		ByteArrayOutputStream pack200buffer = new ByteArrayOutputStream();
		GZIPOutputStream gzipOutput = new GZIPOutputStream(pack200buffer);
		Pack200.newPacker().pack(
				new JarInputStream(new ByteArrayInputStream(
						jarbuffer.toByteArray())), gzipOutput);
		gzipOutput.finish();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int count = instrumenter.instrumentAll(new ByteArrayInputStream(
				pack200buffer.toByteArray()), out);

		jarbuffer.reset();
		Pack200.newUnpacker()
				.unpack(new GZIPInputStream(new ByteArrayInputStream(
						out.toByteArray())), new JarOutputStream(jarbuffer));

		assertEquals(1, count);
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				jarbuffer.toByteArray()));
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	@Test
	public void testInstrumentAll_Other() throws IOException {
		InputStream in = new ByteArrayInputStream("text".getBytes());
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(in, out);

		assertEquals(0, count);
		assertEquals("text", new String(out.toByteArray()));
	}

}
