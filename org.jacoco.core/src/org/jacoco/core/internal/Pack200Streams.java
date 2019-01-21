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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

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
	public static InputStream unpack(final InputStream input)
			throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final JarOutputStream jar = new JarOutputStream(buffer);
		Pack200.newUnpacker().unpack(new NoCloseInput(input), jar);
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
	public static void pack(final byte[] source, final OutputStream output)
			throws IOException {
		final JarInputStream jar = new JarInputStream(new ByteArrayInputStream(
				source));
		Pack200.newPacker().pack(jar, output);
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
