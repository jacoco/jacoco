/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jacoco.cli.internal.CommandTestBase;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.runtime.IRemoteCommandVisitor;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.tools.ExecFileLoader;
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
		int port = startMockServer("a");

		execute("dump", "--destfile", execfile.getAbsolutePath(), "--port",
				String.valueOf(port), "--append", "true");

		assertOk();
		assertContains("[INFO] Connecting to ", out);
		assertContains("[INFO] Writing execution data to "
				+ execfile.getAbsolutePath(), out);

		Set<String> names = loadExecFile(execfile);
		assertEquals(new HashSet<String>(Arrays.asList("a")), names);
	}

	@Test
	public void should_append_to_existing_file_when_append_is_true()
			throws Exception {

		File execfile = new File(tmp.getRoot(), "jacoco.exec");

		int port = startMockServer("a");
		execute("dump", "--destfile", execfile.getAbsolutePath(), "--port",
				String.valueOf(port));
		assertOk();

		port = startMockServer("b");
		execute("dump", "--destfile", execfile.getAbsolutePath(), "--port",
				String.valueOf(port), "--append", "true");
		assertOk();

		Set<String> names = loadExecFile(execfile);
		assertEquals(new HashSet<String>(Arrays.asList("a", "b")), names);
	}

	@Test
	public void should_overwrite_existing_file_when_append_is_false()
			throws Exception {

		File execfile = new File(tmp.getRoot(), "jacoco.exec");

		int port = startMockServer("a");
		execute("dump", "--destfile", execfile.getAbsolutePath(), "--port",
				String.valueOf(port));
		assertOk();

		port = startMockServer("b");
		execute("dump", "--destfile", execfile.getAbsolutePath(), "--port",
				String.valueOf(port), "--append", "false");
		assertOk();

		Set<String> names = loadExecFile(execfile);
		assertEquals(new HashSet<String>(Arrays.asList("b")), names);
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

	private int startMockServer(final String classname) throws IOException {
		serverSocket = new ServerSocket(0, 0, InetAddress.getByName(null));
		new Thread() {
			@Override
			public void run() {
				try {
					serveRequest(serverSocket.accept(), classname);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}.start();
		return serverSocket.getLocalPort();
	}

	private void serveRequest(final Socket socket, final String classname)
			throws IOException {
		final RemoteControlWriter writer = new RemoteControlWriter(
				socket.getOutputStream());
		final RemoteControlReader reader = new RemoteControlReader(
				socket.getInputStream());
		reader.setRemoteCommandVisitor(new IRemoteCommandVisitor() {

			public void visitDumpCommand(boolean dump, boolean reset)
					throws IOException {
				writer.visitClassExecution(
						new ExecutionData(classname.hashCode(), classname,
								new boolean[] { true }));
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

	private Set<String> loadExecFile(File file) throws IOException {
		ExecFileLoader loader = new ExecFileLoader();
		loader.load(file);
		Set<String> names = new HashSet<String>();
		for (ExecutionData d : loader.getExecutionDataStore().getContents()) {
			names.add(d.getName());
		}
		return names;
	}

}
