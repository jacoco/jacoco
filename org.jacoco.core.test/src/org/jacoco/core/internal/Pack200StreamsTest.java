/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.test.TargetLoader;
import org.junit.AssumptionViolatedException;
import org.junit.Test;

/**
 * Unit tests for {@link Pack200Streams}.
 */
public class Pack200StreamsTest {

	@Test
	public void pack_should_pack() throws Exception {
		try {
			Class.forName("java.util.jar.Pack200");
		} catch (ClassNotFoundException e) {
			throw new AssumptionViolatedException(
					"this test requires JDK with Pack200");
		}

		ByteArrayOutputStream jarbuffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(jarbuffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		zipout.write(TargetLoader.getClassDataAsBytes(getClass()));
		zipout.finish();

		ByteArrayOutputStream pack200buffer = new ByteArrayOutputStream();
		Pack200Streams.pack(jarbuffer.toByteArray(),
				new NoCloseOutputStream(pack200buffer));

		jarbuffer.reset();
		final Object unpacker = Class.forName("java.util.jar.Pack200")
				.getMethod("newUnpacker").invoke(null);
		Class.forName("java.util.jar.Pack200$Unpacker")
				.getMethod("unpack", InputStream.class, JarOutputStream.class)
				.invoke(unpacker,
						new ByteArrayInputStream(pack200buffer.toByteArray()),
						new JarOutputStream(jarbuffer));

		ZipInputStream zipin = new ZipInputStream(
				new ByteArrayInputStream(jarbuffer.toByteArray()));
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	@Test
	public void pack_should_throw_IOException_when_can_not_write_to_OutputStream() {
		try {
			Class.forName("java.util.jar.Pack200");
		} catch (ClassNotFoundException e) {
			throw new AssumptionViolatedException(
					"this test requires JDK with Pack200");
		}

		final OutputStream outputStream = new BrokenOutputStream();
		try {
			Pack200Streams.pack(new byte[0], outputStream);
			fail("expected exception");
		} catch (IOException e) {
			assertTrue(e.getCause() instanceof IOException);
			assertEquals("fake broken output stream",
					e.getCause().getMessage());
		}
	}

	@Test
	public void pack_should_throw_IOException_when_Pack200_not_available_in_JDK() {
		try {
			Class.forName("java.util.jar.Pack200");
			throw new AssumptionViolatedException(
					"this test requires JDK without Pack200");
		} catch (ClassNotFoundException ignore) {
		}

		try {
			Pack200Streams.pack(new byte[0], new ByteArrayOutputStream());
			fail("expected exception");
		} catch (IOException e) {
			assertNull(e.getMessage());
			assertTrue(e.getCause() instanceof ClassNotFoundException);
		}
	}

	@Test
	public void unpack_should_unpack() throws Exception {
		try {
			Class.forName("java.util.jar.Pack200");
		} catch (ClassNotFoundException e) {
			throw new AssumptionViolatedException(
					"this test requires JDK with Pack200");
		}

		ByteArrayOutputStream jarbuffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(jarbuffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		zipout.write(TargetLoader.getClassDataAsBytes(getClass()));
		zipout.finish();

		ByteArrayOutputStream pack200buffer = new ByteArrayOutputStream();
		final Object packer = Class.forName("java.util.jar.Pack200")
				.getMethod("newPacker").invoke(null);
		Class.forName("java.util.jar.Pack200$Packer")
				.getMethod("pack", JarInputStream.class, OutputStream.class)
				.invoke(packer, new JarInputStream(
						new ByteArrayInputStream(jarbuffer.toByteArray())),
						pack200buffer);

		InputStream result = Pack200Streams.unpack(new NoCloseInputStream(
				new ByteArrayInputStream(pack200buffer.toByteArray())));

		ZipInputStream zipin = new ZipInputStream(result);
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	@Test
	public void unpack_should_throw_IOException_when_can_not_read_from_InputStream() {
		try {
			Class.forName("java.util.jar.Pack200");
		} catch (ClassNotFoundException e) {
			throw new AssumptionViolatedException(
					"this test requires JDK with Pack200");
		}

		final InputStream inputStream = new BrokenInputStream();
		try {
			Pack200Streams.unpack(inputStream);
			fail("expected exception");
		} catch (IOException e) {
			assertTrue(e.getCause() instanceof IOException);
			assertEquals("fake broken input stream", e.getCause().getMessage());
		}
	}

	@Test
	public void unpack_should_throw_IOException_when_Pack200_not_available_in_JDK() {
		try {
			Class.forName("java.util.jar.Pack200");
			throw new AssumptionViolatedException(
					"this test requires JDK without Pack200");
		} catch (ClassNotFoundException ignore) {
		}

		try {
			Pack200Streams.unpack(new ByteArrayInputStream(new byte[0]));
			fail("expected exception");
		} catch (IOException e) {
			assertNull(e.getMessage());
			assertTrue(e.getCause() instanceof ClassNotFoundException);
		}
	}

	static class NoCloseInputStream extends FilterInputStream {
		public NoCloseInputStream(InputStream in) {
			super(in);
		}

		@Override
		public void close() throws IOException {
			fail();
		}
	}

	static class NoCloseOutputStream extends FilterOutputStream {
		public NoCloseOutputStream(OutputStream out) {
			super(out);
		}

		@Override
		public void close() throws IOException {
			fail();
		}
	}

	private static class BrokenInputStream extends InputStream {
		@Override
		public int read() throws IOException {
			throw new IOException("fake broken input stream");
		}
	}

	private static class BrokenOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			throw new IOException("fake broken output stream");
		}
	}

}
