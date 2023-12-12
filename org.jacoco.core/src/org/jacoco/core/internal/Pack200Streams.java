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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * Internal wrapper for the weird Pack200 Java API to allow usage with streams.
 */
public final class Pack200Streams {

	/**
	 * Unpack a stream in Pack200 format into a stream in JAR/ZIP format.
	 *
	 * @param input
	 *            stream in Pack200 format
	 * @return stream in JAR/ZIP format
	 * @throws IOException
	 *             in case of errors with the streams
	 */
	@SuppressWarnings("resource")
	public static InputStream unpack(final InputStream input)
			throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final JarOutputStream jar = new JarOutputStream(buffer);
		try {
			final Object unpacker = Class.forName("java.util.jar.Pack200")
					.getMethod("newUnpacker").invoke(null);
			Class.forName("java.util.jar.Pack200$Unpacker")
					.getMethod("unpack", InputStream.class,
							JarOutputStream.class)
					.invoke(unpacker, new NoCloseInput(input), jar);
		} catch (ClassNotFoundException e) {
			throw newIOException(e);
		} catch (NoSuchMethodException e) {
			throw newIOException(e);
		} catch (IllegalAccessException e) {
			throw newIOException(e);
		} catch (InvocationTargetException e) {
			throw newIOException(e.getCause());
		}
		jar.finish();
		return new ByteArrayInputStream(buffer.toByteArray());
	}

	/**
	 * Packs a buffer in JAR/ZIP format into a stream in Pack200 format.
	 *
	 * @param source
	 *            source in JAR/ZIP format
	 * @param output
	 *            stream in Pack200 format
	 * @throws IOException
	 *             in case of errors with the streams
	 */
	@SuppressWarnings("resource")
	public static void pack(final byte[] source, final OutputStream output)
			throws IOException {
		final JarInputStream jar = new JarInputStream(
				new ByteArrayInputStream(source));
		try {
			final Object packer = Class.forName("java.util.jar.Pack200")
					.getMethod("newPacker").invoke(null);
			Class.forName("java.util.jar.Pack200$Packer")
					.getMethod("pack", JarInputStream.class, OutputStream.class)
					.invoke(packer, jar, output);
		} catch (ClassNotFoundException e) {
			throw newIOException(e);
		} catch (NoSuchMethodException e) {
			throw newIOException(e);
		} catch (IllegalAccessException e) {
			throw newIOException(e);
		} catch (InvocationTargetException e) {
			throw newIOException(e.getCause());
		}
	}

	private static IOException newIOException(final Throwable cause) {
		final IOException exception = new IOException();
		exception.initCause(cause);
		return exception;
	}

	private static class NoCloseInput extends FilterInputStream {
		protected NoCloseInput(final InputStream in) {
			super(in);
		}

		@Override
		public void close() throws IOException {
			// do not close the underlying stream
		}
	}

	private Pack200Streams() {
	}

}
