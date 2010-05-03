/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Serialization of execution data into binary streams.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecutionDataWriter implements ISessionInfoVisitor,
		IExecutionDataVisitor {

	/** File format version, will be incremented for each incompatible change. */
	public static final char FORMAT_VERSION = 0x1005;

	/** Magic number in header for file format identification. */
	public static final char MAGIC_NUMBER = 0xC0C0;

	/** Block identifier for file headers. */
	public static final byte BLOCK_HEADER = 0x01;

	/** Block identifier for session information. */
	public static final byte BLOCK_SESSIONINFO = 0x10;

	/** Block identifier for execution data of a single class. */
	public static final byte BLOCK_EXECUTIONDATA = 0x11;

	private final CompactDataOutput out;

	/**
	 * Creates a new writer based on the given output stream. Depending on the
	 * nature of the underlying stream output should be buffered as most data is
	 * written in single bytes.
	 * 
	 * @param output
	 *            binary stream to write execution data to
	 */
	public ExecutionDataWriter(final OutputStream output) {
		this.out = new CompactDataOutput(output);
	}

	/**
	 * Writes an file header to identify the stream and its protocol version.
	 * 
	 * @throws IOException
	 */
	public void writeHeader() throws IOException {
		out.writeByte(BLOCK_HEADER);
		out.writeChar(MAGIC_NUMBER);
		out.writeChar(FORMAT_VERSION);
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

	public void visitClassExecution(final long id, final String name,
			final boolean[] data) {
		try {
			out.writeByte(BLOCK_EXECUTIONDATA);
			out.writeLong(id);
			out.writeUTF(name);
			out.writeBooleanArray(data);
		} catch (final IOException e) {
			throw new RuntimeException(e);
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
			new ExecutionDataWriter(buffer).writeHeader();
		} catch (final IOException e) {
			// Must not happen with ByteArrayOutputStream
			throw new AssertionError(e);
		}
		return buffer.toByteArray();
	}

}
