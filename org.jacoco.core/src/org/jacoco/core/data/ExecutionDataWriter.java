/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.jacoco.core.internal.instr.IProbeArray;
import org.jacoco.core.internal.instr.ProbeArrayService;

/**
 * Serialization of execution data into binary streams.
 */
public class ExecutionDataWriter
		implements IHeaderVisitor, ISessionInfoVisitor, IExecutionDataVisitor {

	/** The format version before it is determined. */
	public static final char FORMAT_VERSION_UNKNOWN = 0xFFFF;

	/**
	 * File format version, will be incremented for each incompatible change.
	 */
	public static final char FORMAT_VERSION = 0x1007;

	/**
	 * File format version, will be incremented for each incompatible change.
	 */
	public static final char FORMAT_INT_VERSION = 0x1008;

	/**
	 * File format version, will be incremented for each incompatible change.
	 */
	public static final char FORMAT_DOUBLEINT_VERSION = 0x1009;

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

	private char formatVersion;
	private boolean headerWritten;

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
		formatVersion = ProbeArrayService.getFormatVersion();
		headerWritten = false;
	}

	/**
	 * Writes an file header to identify the stream and its protocol version.
	 * 
	 * @throws IOException
	 *             if the header can't be written
	 */
	protected void writeHeaderIfNeeded() throws IOException {
		if (headerWritten) {
			return;
		}
		out.writeByte(BLOCK_HEADER);
		out.writeChar(MAGIC_NUMBER);
		out.writeChar(formatVersion);
		headerWritten = true;
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

	public void visitHeaderInfo(final HeaderInfo info) {
		if (this.formatVersion == info.getFormatVersion()) {
			return;
		}
		if (headerWritten) {
			throw new IllegalStateException(
					"Can't change formatVersion once headers were written. Attempting to change from 0x"
							+ Integer.toHexString(this.formatVersion) + " to 0x"
							+ Integer.toHexString(info.getFormatVersion()));
		}
		this.formatVersion = info.getFormatVersion();
	}

	public void visitSessionInfo(final SessionInfo info) {
		try {
			writeHeaderIfNeeded();
			out.writeByte(BLOCK_SESSIONINFO);
			out.writeUTF(info.getId());
			out.writeLong(info.getStartTimeStamp());
			out.writeLong(info.getDumpTimeStamp());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void visitClassExecution(final ExecutionData data) {
		try {
			final IProbeArray<?> probes = (IProbeArray<?>) data.getProbes();
			writeHeaderIfNeeded();
			out.writeByte(BLOCK_EXECUTIONDATA);
			out.writeLong(data.getId());
			out.writeUTF(data.getName());
			probes.write(formatVersion, out);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the first bytes of a file that represents a valid execution data
	 * file. In any case every execution data file starts with the three bytes
	 * <code>0x01 0xC0 0xC0</code>.
	 * 
	 * @param info
	 *            the header info to use in creating the file header
	 * 
	 * @return first bytes of a execution data file
	 */
	public static final byte[] getFileHeader(final HeaderInfo info) {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			final ExecutionDataWriter writer = new ExecutionDataWriter(buffer);
			writer.visitHeaderInfo(info);
			writer.writeHeaderIfNeeded();
		} catch (final IOException e) {
			// Must not happen with ByteArrayOutputStream
			throw new AssertionError(e);
		}
		return buffer.toByteArray();
	}

}
