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

	private final DataOutput output;

	/**
	 * Creates a new writer based on the given data output.
	 * 
	 * @param output
	 *            data output to write execution data to
	 */
	public ExecutionDataWriter(final DataOutput output) {
		this.output = output;
	}

	/**
	 * Creates a new writer based on the given output stream.
	 * 
	 * @param output
	 *            binary stream to write execution data to
	 */
	public ExecutionDataWriter(final OutputStream output) {
		this((DataOutput) new DataOutputStream(output));
	}

	public void visitClassExecution(final long id, final boolean[][] blockdata) {
		try {
			output.writeLong(id);
			output.writeInt(blockdata.length);
			for (final boolean[] m : blockdata) {
				output.writeInt(m.length);
				for (final boolean b : m) {
					output.writeBoolean(b);
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
