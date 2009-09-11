/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Serialization of execution data into binary streams.
 * 
 * TODO: Add "magic number" header protocol version, more efficient storage
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecutionDataWriter implements IExecutionDataVisitor {

	/** File format version, will be incremented for each incompatible change. */
	public static final char FORMAT_VERSION = 0x1001;

	/** Block identifier for file headers. */
	public static final byte BLOCK_HEADER = 0x01;

	/** Block identifier for execution data of a single class. */
	public static final byte BLOCK_EXECUTIONDATA = 0x10;

	private final DataOutput output;

	/**
	 * Creates a new writer based on the given data output.
	 * 
	 * @param output
	 *            data output to write execution data to
	 * @throws IOException
	 */
	public ExecutionDataWriter(final DataOutput output) throws IOException {
		this.output = output;
	}

	/**
	 * Creates a new writer based on the given output stream.
	 * 
	 * @param output
	 *            binary stream to write execution data to
	 * @throws IOException
	 */
	public ExecutionDataWriter(final OutputStream output) throws IOException {
		this((DataOutput) new DataOutputStream(output));
	}

	/**
	 * Writes an file header to identify the stream and its protocol version.
	 * 
	 * @throws IOException
	 */
	public void writeHeader() throws IOException {
		output.write(BLOCK_HEADER);
		output.writeChar(FORMAT_VERSION);
	}

	public void visitClassExecution(final long id, final boolean[][] blockdata) {
		try {
			output.write(BLOCK_EXECUTIONDATA);
			output.writeLong(id);
			writeVarInt(blockdata.length);
			for (final boolean[] m : blockdata) {
				writeVarInt(m.length);
				for (final boolean b : m) {
					output.writeBoolean(b);
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes a variable length representation of an integer value that reduces
	 * the number of written bytes for small positive values.
	 * 
	 * @param value
	 *            value to write
	 * @throws IOException
	 */
	protected void writeVarInt(final int value) throws IOException {
		if ((value & 0xFFFFFF80) == 0) {
			output.write(value);
		} else {
			output.write(0x80 | (value & 0x7F));
			writeVarInt(value >>> 7);
		}
	}

}
