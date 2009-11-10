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

import static java.lang.String.format;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Deserialization of execution data from binary streams.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecutionDataReader {

	private final CompactDataInput in;

	private IExecutionDataVisitor executionDataVisitor;

	/**
	 * Creates a new reader based on the given input stream input. Depending on
	 * the nature of the underlying stream input should be buffered as most data
	 * is read in single bytes.
	 * 
	 * @param input
	 *            input stream to read execution data from
	 */
	public ExecutionDataReader(final InputStream input) {
		this.in = new CompactDataInput(input);
	}

	/**
	 * Sets an listener for execution data.
	 * 
	 * @param visitor
	 */
	public void setExecutionDataVisitor(final IExecutionDataVisitor visitor) {
		this.executionDataVisitor = visitor;
	}

	/**
	 * Reads all data and reports it to the corresponding visitors.
	 * 
	 * @throws IOException
	 *             might be thrown by the underlying input stream
	 */
	public void read() throws IOException {
		try {
			while (true) {
				final byte block = in.readByte();
				switch (block) {
				case ExecutionDataWriter.BLOCK_HEADER:
					readHeader();
					break;
				case ExecutionDataWriter.BLOCK_EXECUTIONDATA:
					readExecutionData();
					break;
				default:
					throw new IOException(format("Unknown block type %x.",
							Integer.valueOf(block)));
				}
			}
		} catch (final EOFException e) {
			// expected at the end of the stream
		}
	}

	private void readHeader() throws IOException {
		if (in.readChar() != ExecutionDataWriter.MAGIC_NUMBER) {
			throw new IOException("Invalid execution data file.");
		}
		final char version = in.readChar();
		if (version != ExecutionDataWriter.FORMAT_VERSION) {
			throw new IOException(format("Incompatible format version %x.",
					Integer.valueOf(version)));
		}
	}

	private void readExecutionData() throws IOException {
		final long classid = in.readLong();
		final String name = in.readUTF();
		final boolean[] data = new boolean[in.readVarInt()];
		for (int i = 0; i < data.length; i++) {
			data[i] = in.readPackedBoolean();
		}
		in.finishPackedBoolean();
		if (executionDataVisitor != null) {
			executionDataVisitor.visitClassExecution(classid, name, data);
		}
	}

}
