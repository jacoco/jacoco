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
package org.jacoco.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import java.util.jar.Pack200;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.test.TargetLoader;
import org.junit.Test;

/**
 * Unit tests for {@link Pack200Streams}.
 */
public class Pack200StreamsTest {

	@Test
	public void testPack() throws IOException {
		ByteArrayOutputStream jarbuffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(jarbuffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		zipout.write(TargetLoader.getClassDataAsBytes(getClass()));
		zipout.finish();

		ByteArrayOutputStream pack200buffer = new ByteArrayOutputStream();
		Pack200Streams.pack(jarbuffer.toByteArray(), new NoCloseOutputStream(
				pack200buffer));

		jarbuffer.reset();
		Pack200.newUnpacker().unpack(
				new ByteArrayInputStream(pack200buffer.toByteArray()),
				new JarOutputStream(jarbuffer));

		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				jarbuffer.toByteArray()));
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
	}

	@Test
	public void testUnpack() throws IOException {
		ByteArrayOutputStream jarbuffer = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(jarbuffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		zipout.write(TargetLoader.getClassDataAsBytes(getClass()));
		zipout.finish();

		ByteArrayOutputStream pack200buffer = new ByteArrayOutputStream();
		Pack200.newPacker().pack(
				new JarInputStream(new ByteArrayInputStream(
						jarbuffer.toByteArray())), pack200buffer);

		InputStream result = Pack200Streams.unpack(new NoCloseInputStream(
				new ByteArrayInputStream(pack200buffer.toByteArray())));

		ZipInputStream zipin = new ZipInputStream(result);
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNull(zipin.getNextEntry());
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

}
