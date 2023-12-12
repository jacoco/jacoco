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
package org.jacoco.agent.rt.internal.output;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.jacoco.core.runtime.IRemoteCommandVisitor;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Handler for a single socket based remote connection.
 */
class TcpConnection implements IRemoteCommandVisitor {

	private final RuntimeData data;

	private final Socket socket;

	private RemoteControlWriter writer;

	private RemoteControlReader reader;

	private boolean initialized;

	public TcpConnection(final Socket socket, final RuntimeData data) {
		this.socket = socket;
		this.data = data;
		this.initialized = false;
	}

	public void init() throws IOException {
		this.writer = new RemoteControlWriter(socket.getOutputStream());
		this.reader = new RemoteControlReader(socket.getInputStream());
		this.reader.setRemoteCommandVisitor(this);
		this.initialized = true;
	}

	/**
	 * Processes all requests for this session until the socket is closed.
	 *
	 * @throws IOException
	 *             in case of problems whith the connection
	 */
	public void run() throws IOException {
		try {
			while (reader.read()) {
			}
		} catch (final SocketException e) {
			// If the local socket is closed while polling for commands the
			// SocketException is expected.
			if (!socket.isClosed()) {
				throw e;
			}
		} finally {
			close();
		}
	}

	/**
	 * Dumps the current execution data if the connection is already initialized
	 * and the underlying socket is still open.
	 *
	 * @param reset
	 *            if <code>true</code> execution data is cleared afterwards
	 * @throws IOException
	 */
	public void writeExecutionData(final boolean reset) throws IOException {
		if (initialized && !socket.isClosed()) {
			visitDumpCommand(true, reset);
		}
	}

	/**
	 * Closes the underlying socket if not closed yet.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (!socket.isClosed()) {
			socket.close();
		}
	}

	// === IRemoteCommandVisitor ===

	public void visitDumpCommand(final boolean dump, final boolean reset)
			throws IOException {
		if (dump) {
			data.collect(writer, writer, reset);
		} else {
			if (reset) {
				data.reset();
			}
		}
		writer.sendCmdOk();
	}

}
