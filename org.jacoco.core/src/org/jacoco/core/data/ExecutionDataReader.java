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

import java.io.DataInput;
import java.io.DataInputStream;
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

	private final DataInput input;

	private IExecutionDataVisitor executionDataVisitor;

	/**
	 * Creates a new reader based on the given data input.
	 * 
	 * @param input
	 *            data input to read execution data from
	 */
	public ExecutionDataReader(final DataInput input) {
		this.input = input;
	}

	/**
	 * Creates a new reader based on the given input stream input.
	 * 
	 * @param input
	 *            input stream to read execution data from
	 */
	public ExecutionDataReader(final InputStream input) {
		this((DataInput) new DataInputStream(input));
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
				final byte block = input.readByte();
				switch (block) {
				case ExecutionDataWriter.BLOCK_HEADER:
					readHeader();
					break;
				case ExecutionDataWriter.BLOCK_EXECUTIONDATA:
					readExecutionData();
					break;
				default:
					throw new IOException("Unknown block type "
							+ Integer.toHexString(block));
				}
			}
		} catch (final EOFException e) {
			// expected at the end of the stream
		}
	}

	private void readHeader() throws IOException {
		final char version = input.readChar();
		if (version != ExecutionDataWriter.FORMAT_VERSION) {
			throw new IOException("Incompatible format version "
					+ Integer.toHexString(version));
		}
	}

	private void readExecutionData() throws IOException {
		final long classid = input.readLong();
		final boolean[][] blockdata = new boolean[input.readInt()][];
		for (int i = 0; i < blockdata.length; i++) {
			blockdata[i] = new boolean[input.readInt()];
			for (int j = 0; j < blockdata[i].length; j++) {
				blockdata[i][j] = input.readBoolean();
			}
		}
		if (executionDataVisitor != null) {
			executionDataVisitor.visitClassExecution(classid, blockdata);
		}
	}

}
