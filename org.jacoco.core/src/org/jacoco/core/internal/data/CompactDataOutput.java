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
package org.jacoco.core.internal.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Additional data output methods for compact storage of data structures.
 *
 * @see CompactDataInput
 */
public class CompactDataOutput extends DataOutputStream {

	/**
	 * Creates a new {@link CompactDataOutput} instance that writes data to the
	 * specified underlying output stream
	 *
	 * @param out
	 *            underlying output stream
	 */
	public CompactDataOutput(final OutputStream out) {
		super(out);
	}

	/**
	 * Writes a variable length representation of an integer value that reduces
	 * the number of written bytes for small positive values. Depending on the
	 * given value 1 to 5 bytes will be written to the underlying stream.
	 *
	 * @param value
	 *            value to write
	 * @throws IOException
	 *             if thrown by the underlying stream
	 */
	public void writeVarInt(final int value) throws IOException {
		if ((value & 0xFFFFFF80) == 0) {
			writeByte(value);
		} else {
			writeByte(0x80 | (value & 0x7F));
			writeVarInt(value >>> 7);
		}
	}

	/**
	 * Writes a boolean array. Internally a sequence of boolean values is packed
	 * into single bits.
	 *
	 * @param value
	 *            boolean array
	 * @throws IOException
	 *             if thrown by the underlying stream
	 */
	public void writeBooleanArray(final boolean[] value) throws IOException {
		writeVarInt(value.length);
		int buffer = 0;
		int bufferSize = 0;
		for (final boolean b : value) {
			if (b) {
				buffer |= 0x01 << bufferSize;
			}
			if (++bufferSize == 8) {
				writeByte(buffer);
				buffer = 0;
				bufferSize = 0;
			}
		}
		if (bufferSize > 0) {
			writeByte(buffer);
		}
	}

}
