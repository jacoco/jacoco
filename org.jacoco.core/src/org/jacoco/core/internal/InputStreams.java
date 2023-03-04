/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for {@link InputStream}s.
 */
public final class InputStreams {

	private InputStreams() {
	}

	/**
	 * Reads all bytes from an input stream into a byte array. The provided
	 * {@link InputStream} is not closed by this method.
	 *
	 * @param is
	 *            the input stream to read from
	 * @return a byte array containing all the bytes from the stream
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static byte[] readFully(final InputStream is) throws IOException {
		final byte[] buf = new byte[1024];
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (true) {
			final int r = is.read(buf);
			if (r == -1) {
				break;
			}
			out.write(buf, 0, r);
		}
		return out.toByteArray();
	}

}
