/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.Opcodes;

/**
 * Utilities to read and modify bytecode version in bytes of class. Main purpose
 * of this class is to deal with bytecode versions which are not yet supported
 * by ASM.
 */
public final class BytecodeVersion {

	private static final int VERSION_INDEX = 6;

	/**
	 * Version of the Java 10 class file format.
	 */
	public static final int V10 = Opcodes.V9 + 1;

	private BytecodeVersion() {
	}

	/**
	 * Gets major of bytecode version number from given bytes of class.
	 * 
	 * @param b
	 *            bytes of class
	 * @return version of bytecode
	 */
	public static int get(final byte[] b) {
		return (short) (((b[VERSION_INDEX] & 0xFF) << 8)
				| (b[VERSION_INDEX + 1] & 0xFF));
	}

	/**
	 * Sets major of bytecode version in given bytes of class.
	 *
	 * @param b
	 *            bytes of class
	 * @param version
	 *            version of bytecode to set
	 */
	public static void set(final byte[] b, final int version) {
		b[VERSION_INDEX] = (byte) (version >>> 8);
		b[VERSION_INDEX + 1] = (byte) version;
	}

	/**
	 * Returns given bytes of class if its major bytecode version is less that
	 * {@link #V10}, otherwise returns copy where major version set to
	 * {@link Opcodes#V9}.
	 * 
	 * @param version
	 *            version of bytecode
	 * @param source
	 *            bytes of class
	 * @return given bytes of class if version is less than {@link #V10},
	 *         otherwise copy where version set to {@link Opcodes#V9}
	 */
	public static byte[] downgradeIfNeeded(final int version,
			final byte[] source) {
		if (V10 != version) {
			return source;
		}
		final byte[] b = new byte[source.length];
		System.arraycopy(source, 0, b, 0, source.length);
		set(b, Opcodes.V9);
		return b;
	}

}
