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
package org.jacoco.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * This example starts a socket server to collect coverage from agents that run
 * in output mode <code>tcpclient</code>. The collected data is dumped to a
 * local file.
 */
public final class ExecutionDataServer {

	private static final String DESTFILE = "jacoco-server.exec";

	private static final String ADDRESS = "localhost";

	private static final int PORT = 6300;

	/**
	 * Start the server as a standalone program.
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		final ExecutionDataWriter fileWriter = new ExecutionDataWriter(
				new FileOutputStream(DESTFILE));
		final ServerSocket server = new ServerSocket(PORT, 0,
				InetAddress.getByName(ADDRESS));
		while (true) {
			final Handler handler = new Handler(server.accept(), fileWriter);
			new Thread(handler).start();
		}
	}

	private static class Handler
			implements Runnable, ISessionInfoVisitor, IExecutionDataVisitor {

		private final Socket socket;

		private final RemoteControlReader reader;

		private final ExecutionDataWriter fileWriter;

		Handler(final Socket socket, final ExecutionDataWriter fileWriter)
				throws IOException {
			this.socket = socket;
			this.fileWriter = fileWriter;

			// Just send a valid header:
			new RemoteControlWriter(socket.getOutputStream());

			reader = new RemoteControlReader(socket.getInputStream());
			reader.setSessionInfoVisitor(this);
			reader.setExecutionDataVisitor(this);
		}

		public void run() {
			try {
				while (reader.read()) {
				}
				socket.close();
				synchronized (fileWriter) {
					fileWriter.flush();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		public void visitSessionInfo(final SessionInfo info) {
			System.out.printf("Retrieving execution Data for session: %s%n",
					info.getId());
			synchronized (fileWriter) {
				fileWriter.visitSessionInfo(info);
			}
		}

		public void visitClassExecution(final ExecutionData data) {
			synchronized (fileWriter) {
				fileWriter.visitClassExecution(data);
			}
		}
	}

	private ExecutionDataServer() {
	}
}
