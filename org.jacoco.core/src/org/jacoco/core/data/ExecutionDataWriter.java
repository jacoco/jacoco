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
package org.jacoco.core.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jacoco.core.internal.data.CompactDataOutput;

/**
 * Serialization of execution data into binary streams.
 */
public class ExecutionDataWriter
		implements ISessionInfoVisitor, IExecutionDataVisitor {

	/**
	 * File format version, will be incremented for each incompatible change.
	 */
	public static final char FORMAT_VERSION;

	static {
		// Runtime initialize to ensure javac does not inline the value.
		FORMAT_VERSION = 0x1007;
	}

	/** Magic number in header for file format identification. */
	public static final char MAGIC_NUMBER = 0xC0C0;

	/** Block identifier for file headers. */
	public static final byte BLOCK_HEADER = 0x01;

	/** Block identifier for session information. */
	public static final byte BLOCK_SESSIONINFO = 0x10;

	/** Block identifier for execution data of a single class. */
	public static final byte BLOCK_EXECUTIONDATA = 0x11;

	/** Underlying data output */
	protected final CompactDataOutput out;

	/**
	 * Creates a new writer based on the given output stream. Depending on the
	 * nature of the underlying stream output should be buffered as most data is
	 * written in single bytes.
	 *
	 * @param output
	 *            binary stream to write execution data to
	 * @throws IOException
	 *             if the header can't be written
	 */
	public ExecutionDataWriter(final OutputStream output) throws IOException {
		this.out = new CompactDataOutput(output);
		writeHeader();
	}

	/**
	 * Writes an file header to identify the stream and its protocol version.
	 *
	 * @throws IOException
	 *             if the header can't be written
	 */
	private void writeHeader() throws IOException {
		out.writeByte(BLOCK_HEADER);
		out.writeChar(MAGIC_NUMBER);
		out.writeChar(FORMAT_VERSION);
	}

	/**
	 * Flushes the underlying stream.
	 *
	 * @throws IOException
	 *             if the underlying stream can't be flushed
	 */
	public void flush() throws IOException {
		out.flush();
	}

	public void visitSessionInfo(final SessionInfo info) {
		try {
			out.writeByte(BLOCK_SESSIONINFO);
			out.writeUTF(info.getId());
			out.writeLong(info.getStartTimeStamp());
			out.writeLong(info.getDumpTimeStamp());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void visitClassExecution(final ExecutionData data) {
		if (data.hasHits()) {
			try {
				out.writeByte(BLOCK_EXECUTIONDATA);
				out.writeLong(data.getId());
				out.writeUTF(data.getName());
				out.writeBooleanArray(data.getProbes());
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Returns the first bytes of a file that represents a valid execution data
	 * file. In any case every execution data file starts with the three bytes
	 * <code>0x01 0xC0 0xC0</code>.
	 *
	 * @return first bytes of a execution data file
	 */
	public static final byte[] getFileHeader() {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			new ExecutionDataWriter(buffer);
		} catch (final IOException e) {
			// Must not happen with ByteArrayOutputStream
			throw new AssertionError(e);
		}
		return buffer.toByteArray();
	}

}
