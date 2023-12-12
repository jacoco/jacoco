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
package org.jacoco.core.runtime;

import java.io.IOException;
import java.io.OutputStream;

import org.jacoco.core.data.ExecutionDataWriter;

/**
 * {@link ExecutionDataWriter} with commands added for runtime remote control.
 */
public class RemoteControlWriter extends ExecutionDataWriter
		implements IRemoteCommandVisitor {

	/** Block identifier to confirm successful command execution. */
	public static final byte BLOCK_CMDOK = 0x20;

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
	 *
	 * @throws IOException
	 *             in case of problems with the remote connection
	 */
	public void sendCmdOk() throws IOException {
		out.writeByte(RemoteControlWriter.BLOCK_CMDOK);
	}

	public void visitDumpCommand(final boolean dump, final boolean reset)
			throws IOException {
		out.writeByte(RemoteControlWriter.BLOCK_CMDDUMP);
		out.writeBoolean(dump);
		out.writeBoolean(reset);
	}

}
