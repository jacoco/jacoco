/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.jacoco.core.data.ExecutionDataReader;

/**
 * {@link ExecutionDataReader} with commands added for runtime remote control.
 */
public class RemoteControlReader extends ExecutionDataReader {

	private IRemoteCommandVisitor remoteCommandVisitor;

	/**
	 * Create a new read based on the given input stream.
	 * 
	 * @param input
	 *            input stream to read commands from
	 * @throws IOException
	 *             if the stream does not have a valid header
	 */
	public RemoteControlReader(final InputStream input) throws IOException {
		super(input);
	}

	@Override
	protected boolean readBlock(final byte blockid) throws IOException {
		switch (blockid) {
		case RemoteControlWriter.BLOCK_CMDDUMP:
			readDumpCommand();
			return true;
		case RemoteControlWriter.BLOCK_CMDOK:
			return false;
		default:
			return super.readBlock(blockid);
		}
	}

	/**
	 * Sets an listener for agent commands.
	 * 
	 * @param visitor
	 *            visitor to retrieve agent commands
	 */
	public void setRemoteCommandVisitor(final IRemoteCommandVisitor visitor) {
		this.remoteCommandVisitor = visitor;
	}

	private void readDumpCommand() throws IOException {
		if (remoteCommandVisitor == null) {
			throw new IOException("No remote command visitor.");
		}
		final boolean dump = in.readBoolean();
		final boolean reset = in.readBoolean();
		remoteCommandVisitor.visitDumpCommand(dump, reset);
	}

}
