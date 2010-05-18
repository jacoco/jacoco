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
package org.jacoco.core.runtime;

import java.io.IOException;
import java.io.OutputStream;

import org.jacoco.core.data.ExecutionDataWriter;

/**
 * {@link ExecutionDataWriter} with commands added for runtime remote control.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class RemoteControlWriter extends ExecutionDataWriter implements
		IRemoteCommandVisitor {

	/** Block identifier to confirm successful command execution. */
	public static final byte BLOCK_CMDOK = 0x20;

	/** Block identifier to announce that the connection will be closed. */
	public static final byte BLOCK_CMDCLOSE = 0x2f;

	/** Block identifier for dump command */
	public static final byte BLOCK_CMDDUMP = 0x40;

	/**
	 * Creates a new writer based on the given output stream.
	 * 
	 * @param output
	 *            stream to write commands to
	 * @throws IOException
	 *             if the header can't be written
	 */
	public RemoteControlWriter(final OutputStream output) throws IOException {
		super(output);
	}

	/**
	 * Sends a confirmation that a commands has been successfully executed and
	 * the response is completed.
	 */
	public void sendCmdOk() {
		try {
			out.writeByte(RemoteControlWriter.BLOCK_CMDOK);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sends a announcement that the stream connection will be closed
	 * afterwards.
	 * 
	 * @throws IOException
	 */
	public void sendCmdClose() throws IOException {
		out.writeByte(RemoteControlWriter.BLOCK_CMDCLOSE);
	}

	public void visitDumpCommand(final boolean dump, final boolean reset) {
		try {
			out.writeByte(RemoteControlWriter.BLOCK_CMDDUMP);
			out.writeBoolean(dump);
			out.writeBoolean(reset);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
