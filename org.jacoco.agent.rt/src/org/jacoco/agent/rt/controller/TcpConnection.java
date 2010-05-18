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
package org.jacoco.agent.rt.controller;

import java.io.IOException;
import java.net.Socket;

import org.jacoco.core.runtime.IRemoteCommandVisitor;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Handler for a single socket based remote connection.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
class TcpConnection implements IRemoteCommandVisitor {

	private final IRuntime runtime;

	private final Socket socket;

	private final RemoteControlWriter writer;

	private final RemoteControlReader reader;

	public TcpConnection(Socket socket, IRuntime runtime) throws IOException {
		this.socket = socket;
		this.runtime = runtime;
		this.writer = new RemoteControlWriter(socket.getOutputStream());
		this.reader = new RemoteControlReader(socket.getInputStream());
		this.reader.setRemoteCommandVisitor(this);
	}

	/**
	 * Processes all requests for this session until the socket is closed.
	 */
	public void run() throws IOException {
		try {
			reader.read();
		} finally {
			socket.close();
		}
	}

	/**
	 * Dumps the current execution data if the underlying socket is still open.
	 * 
	 * @throws IOException
	 */
	public void writeExecutionData() throws IOException {
		if (!socket.isClosed()) {
			visitDumpCommand(true, false);
		}
	}

	/**
	 * Closes the underlying socket if not closed yet.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (!socket.isClosed()) {
			writer.sendCmdClose();
			socket.shutdownInput();
		}
	}

	public void visitDumpCommand(boolean dump, boolean reset) {
		if (dump) {
			runtime.collect(writer, writer, reset);
		} else {
			if (reset) {
				runtime.reset();
			}
		}
		writer.sendCmdOk();
	}

}
