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
package org.jacoco.cli.internal.commands;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.jacoco.cli.internal.CommandTestBase;
import org.jacoco.core.runtime.IRemoteCommandVisitor;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link Dump}.
 */
public class DumpTest extends CommandTestBase {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private ServerSocket serverSocket;

	@After
	public void after() throws IOException {
		if (serverSocket != null) {
			serverSocket.close();
		}
	}

	@Test
	public void should_print_usage_when_no_argument_is_given()
			throws Exception {
		execute("dump");
		assertFailure();
		assertContains("\"--destfile\"", err);
		assertContains("java -jar jacococli.jar dump [--address <address>]",
				err);
	}

	@Test
	public void should_write_dump() throws Exception {

		File execfile = new File(tmp.getRoot(), "jacoco.exec");
		int port = startMockServer();

		execute("dump", "--destfile", execfile.getAbsolutePath(), "--port",
				String.valueOf(port));

		assertOk();
		assertContains("[INFO] Connecting to ", out);
		assertContains("[INFO] Writing execution data to "
				+ execfile.getAbsolutePath(), out);
		assertTrue(execfile.exists());
	}

	@Test
	public void should_log_connection_error_when_retry_is_specified()
			throws Exception {

		File execfile = new File(tmp.getRoot(), "jacoco.exec");
		int port = unusedPort();

		try {
			execute("dump", "--destfile", execfile.getAbsolutePath(), "--port",
					String.valueOf(port), "--retry", "1");
			fail("IOException expected");
		} catch (IOException ignore) {
		}

		// Locale independent parts of error message:
		assertContains("[WARN]", err);
		assertContains("Connection refused", err);
	}

	private int startMockServer() throws IOException {
		serverSocket = new ServerSocket(0, 0, InetAddress.getByName(null));
		new Thread() {
			@Override
			public void run() {
				try {
					serveRequest(serverSocket.accept());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}.start();
		return serverSocket.getLocalPort();
	}

	private void serveRequest(Socket socket) throws IOException {
		final RemoteControlWriter writer = new RemoteControlWriter(
				socket.getOutputStream());
		final RemoteControlReader reader = new RemoteControlReader(
				socket.getInputStream());
		reader.setRemoteCommandVisitor(new IRemoteCommandVisitor() {

			public void visitDumpCommand(boolean dump, boolean reset)
					throws IOException {
				writer.sendCmdOk();
			}
		});
		while (reader.read()) {
		}
	}

	private int unusedPort() throws IOException {
		final ServerSocket serverSocket = new ServerSocket(0, 0,
				InetAddress.getByName(null));
		final int port = serverSocket.getLocalPort();
		serverSocket.close();
		return port;
	}

}
