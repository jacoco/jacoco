/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.Opcodes;

/**
 * Patching for Java 9 classes, so that ASM can read them.
 */
public final class Java9Support {

	/**
	 * Version of the Java 9 class file format.
	 */
	public static final int V1_9 = Opcodes.V1_8 + 1;

	private Java9Support() {
	}

	/**
	 * Reads all bytes from an input stream into a byte array.
	 *
	 * @param is
	 *             the input stream to read from
	 * @return a byte array containing all the bytes from the stream
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static byte[] readFully(final InputStream is)
			throws IOException {
		if (is == null) {
			throw new IllegalArgumentException();
		}
		final byte[] buf = new byte[1024];
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (true) {
			int r = is.read(buf);
			if (r == -1) {
				break;
			}
			out.write(buf, 0, r);
		}
		return out.toByteArray();
	}

	private static void putShort(byte[] b, int index, int s) {
		b[index] = (byte) (s >>> 8);
		b[index + 1] = (byte) s;
	}

	private static short readShort(byte[] b, int index) {
		return (short) (((b[index] & 0xFF) << 8) | (b[index + 1] & 0xFF));
	}

	/**
	 * Determines whether class definition contains {@link #V1_9} version.
	 *
	 * @param buffer
	 *             definition of the class
	 * @return <code>true</code> if class definition contains Java 9 version
	 */
	public static boolean isPatchRequired(byte[] buffer) {
		return readShort(buffer, 6) == V1_9;
	}

	/**
	 * Returns new definition of class with version {@link Opcodes#V1_8},
	 * if it has version {@link #V1_9}.
	 *
	 * @param buffer
	 *             definition of the class
	 * @return new definition of the class
	 */
	public static byte[] downgradeIfRequired(byte[] buffer) {
		return isPatchRequired(buffer) ? downgrade(buffer) : buffer;
	}

	/**
	 * Replaces version in the definition of class on {@link Opcodes#V1_8}.
	 *
	 * @param b
	 *             definition of the class
	 * @return new definition of the class
	 */
	public static byte[] downgrade(byte[] b) {
		byte[] result = new byte[b.length];
		System.arraycopy(b, 0, result, 0, b.length);
		putShort(result, 6, Opcodes.V1_8);
		return result;
	}

	/**
	 * Replaces version in the definition of class on {@link #V1_9}.
	 *
	 * @param b
	 *             definition of the class
	 */
	public static void upgrade(byte[] b) {
		putShort(b, 6, V1_9);
	}

}
