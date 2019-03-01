/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import static org.junit.Assert.assertArrayEquals;
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
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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

	private static final class AccessorGenerator
			implements IExecutionDataAccessorGenerator {

		long classId;

		public int generateDataAccessor(final long classId,
				final String classname, final int probeCount,
				final MethodVisitor mv) {
			this.classId = classId;
			InstrSupport.push(mv, probeCount);
			mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
			return 1;
		}

	}

	private AccessorGenerator accessorGenerator;
	private Instrumenter instrumenter;

	@Before
	public void setup() throws Exception {
		accessorGenerator = new AccessorGenerator();
		instrumenter = new Instrumenter(accessorGenerator);
	}

	@Test
	public void should_not_modify_class_bytes_to_support_next_version()
			throws Exception {
		final byte[] originalBytes = createClass(Opcodes.V12 + 1);
		final byte[] bytes = new byte[originalBytes.length];
		System.arraycopy(originalBytes, 0, bytes, 0, originalBytes.length);
		final long expectedClassId = CRC64.classId(bytes);

		instrumenter.instrument(bytes, "");

		assertArrayEquals(originalBytes, bytes);
		assertEquals(expectedClassId, accessorGenerator.classId);
	}

	private static byte[] createClass(final int version) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(version, 0, "Foo", null, "java/lang/Object", null);
		cw.visitEnd();
		return cw.toByteArray();
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

	@Test
	public void testInstrumentAll_Class() throws IOException {
		InputStream in = TargetLoader.getClassData(getClass());
		OutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(in, out, "Test");

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
				new ByteArrayInputStream(buffer.toByteArray()), out, "Test");

		assertEquals(1, count);
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				out.toByteArray()));
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	/**
	 * Triggers exception in
	 * {@link org.jacoco.core.internal.ContentTypeDetector#ContentTypeDetector(InputStream)}.
	 */
	@Test
	public void testInstrumentAll_Broken() {
		try {
			instrumenter.instrumentAll(new BrokenInputStream(),
					new ByteArrayOutputStream(), "Broken");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Broken.", e.getMessage());
		}
	}

	/**
	 * Triggers exception in
	 * {@link Instrumenter#copy(InputStream, OutputStream)}.
	 */
	@Test
	public void testInstrumentAll_Broken2() {
		final InputStream inputStream = new InputStream() {
			private int count;

			@Override
			public int read() throws IOException {
				count++;
				if (count > 4) {
					throw new IOException();
				}
				return 0;
			}
		};

		try {
			instrumenter.instrumentAll(inputStream, new ByteArrayOutputStream(),
					"Broken");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Broken.", e.getMessage());
		}
	}

	/**
	 * Triggers exception in
	 * {@link Instrumenter#nextEntry(ZipInputStream, String)}.
	 */
	@Test
	public void testInstrumentAll_BrokenZip() {
		final byte[] buffer = new byte[30];
		buffer[0] = 0x50;
		buffer[1] = 0x4b;
		buffer[2] = 0x03;
		buffer[3] = 0x04;
		Arrays.fill(buffer, 4, buffer.length, (byte) 0x42);

		try {
			instrumenter.instrumentAll(new ByteArrayInputStream(buffer),
					new ByteArrayOutputStream(), "Test.zip");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Test.zip.", e.getMessage());
		}
	}

	/**
	 * With JDK <= 6 triggers exception in
	 * {@link Instrumenter#copy(InputStream, OutputStream)}.
	 *
	 * With JDK > 6 triggers exception in
	 * {@link org.jacoco.core.internal.ContentTypeDetector#ContentTypeDetector(InputStream)}.
	 */
	@Test
	public void testInstrumentAll_BrokenZipEntry() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(out);
		zip.putNextEntry(new ZipEntry("brokenentry.txt"));
		out.write(0x23); // Unexpected data here
		zip.close();

		try {
			instrumenter.instrumentAll(
					new ByteArrayInputStream(out.toByteArray()),
					new ByteArrayOutputStream(), "broken.zip");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals(
					"Error while instrumenting broken.zip@brokenentry.txt.",
					e.getMessage());
		}
	}

	@Test
	public void testInstrumentAll_BrokenClassFileInZip() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(buffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		final byte[] brokenclass = TargetLoader.getClassDataAsBytes(getClass());
		brokenclass[10] = 0x23;
		zipout.write(brokenclass);
		zipout.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			instrumenter.instrumentAll(
					new ByteArrayInputStream(buffer.toByteArray()), out,
					"test.zip");
			fail();
		} catch (IOException e) {
			assertEquals("Error while instrumenting test.zip@Test.class.",
					e.getMessage());
		}
	}

	/**
	 * Triggers exception in
	 * {@link Instrumenter#instrumentGzip(InputStream, OutputStream, String)}.
	 */
	@Test
	public void testInstrumentAll_BrokenGZ() {
		final byte[] buffer = new byte[] { 0x1f, (byte) 0x8b, 0x00, 0x00 };

		try {
			instrumenter.instrumentAll(new ByteArrayInputStream(buffer),
					new ByteArrayOutputStream(), "Test.gz");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Test.gz.", e.getMessage());
		}
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
				pack200buffer.toByteArray()), out, "Test");

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

	/**
	 * Triggers exception in
	 * {@link Instrumenter#instrumentPack200(InputStream, OutputStream, String)}.
	 */
	@Test
	public void testInstrumentAll_BrokenPack200() {
		final byte[] buffer = new byte[] { (byte) 0xca, (byte) 0xfe,
				(byte) 0xd0, 0x0d };

		try {
			instrumenter.instrumentAll(new ByteArrayInputStream(buffer),
					new ByteArrayOutputStream(), "Test.pack200");
		} catch (IOException e) {
			assertEquals("Error while instrumenting Test.pack200.",
					e.getMessage());
		}
	}

	@Test
	public void testInstrumentAll_Other() throws IOException {
		InputStream in = new ByteArrayInputStream("text".getBytes());
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(in, out, "Test");

		assertEquals(0, count);
		assertEquals("text", new String(out.toByteArray()));
	}

	@Test
	public void testInstrumentAll_RemoveSignatures() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(buffer);
		zipout.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
		zipout.putNextEntry(new ZipEntry("META-INF/ALIAS.SF"));
		zipout.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int count = instrumenter.instrumentAll(
				new ByteArrayInputStream(buffer.toByteArray()), out, "Test");

		assertEquals(0, count);
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				out.toByteArray()));
		assertEquals("META-INF/MANIFEST.MF", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	@Test
	public void testInstrumentAll_KeepSignatures() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(buffer);
		zipout.putNextEntry(new ZipEntry("META-INF/ALIAS.SF"));
		zipout.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		instrumenter.setRemoveSignatures(false);
		int count = instrumenter.instrumentAll(
				new ByteArrayInputStream(buffer.toByteArray()), out, "Test");

		assertEquals(0, count);
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				out.toByteArray()));
		assertEquals("META-INF/ALIAS.SF", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

}
