/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.runtime.IRemoteCommandVisitor;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ExecDumpClient}.
 */
public class ExecDumpClientTest {

	private ExecDumpClient client;
	private List<String> callbacks;

	private boolean dumpRequested;
	private boolean resetRequested;

	private ServerSocket server;

	@Before
	public void setup() {
		callbacks = new ArrayList<String>();
		client = new ExecDumpClient() {
			@Override
			protected void onConnecting(InetAddress address, int port) {
				callbacks.add("onConnecting");
			}

			@Override
			protected void onConnectionFailure(IOException exception) {
				callbacks.add("onConnectionFailure");
			}
		};
	}

	@After
	public void teardown() throws IOException {
		if (server != null) {
			server.close();
		}
	}

	@Test
	public void testNoRetries() throws IOException {
		try {
			client.dump((String) null, getFreePort());
			fail("ConnectException expected");
		} catch (ConnectException e) {
			// expected
		}

		assertEquals(Arrays.asList("onConnecting"), callbacks);
	}

	@Test
	public void testWithRetries() throws IOException {
		client.setRetryCount(3);
		client.setRetryDelay(0);
		try {
			client.dump((String) null, getFreePort());
			fail("ConnectException expected");
		} catch (ConnectException e) {
			// expected
		}

		assertEquals(Arrays.asList( // Initial attempt
				"onConnecting",
				// 1. Retry
				"onConnectionFailure", "onConnecting",
				// 2. Retry
				"onConnectionFailure", "onConnecting",
				// 3. Retry
				"onConnectionFailure", "onConnecting")

		, callbacks);
	}

	@Test
	public void testDump() throws IOException {
		int port = createExecServer();
		ExecFileLoader loader = client.dump((String) null, port);
		assertTrue(dumpRequested);
		assertFalse(resetRequested);

		List<SessionInfo> infos = loader.getSessionInfoStore().getInfos();
		assertEquals(1, infos.size());
		assertEquals("TestId", infos.get(0).getId());
	}

	@Test
	public void testReset() throws IOException {
		int port = createExecServer();
		client.setDump(false);
		client.setReset(true);
		client.dump((String) null, port);
		assertFalse(dumpRequested);
		assertTrue(resetRequested);
	}

	private int getFreePort() throws IOException {
		final ServerSocket server = new ServerSocket(0, 0,
				InetAddress.getByName(null));
		final int port = server.getLocalPort();
		server.close();
		return port;
	}

	private int createExecServer() throws IOException {
		server = new ServerSocket(0, 0, InetAddress.getByName(null));
		new Thread(new Runnable() {
			public void run() {
				try {
					handleConnection(server.accept());
				} catch (IOException e) {
					// ignore
				}
			}
		}).start();
		return server.getLocalPort();
	}

	private void handleConnection(Socket socket) throws IOException {
		final RemoteControlWriter writer = new RemoteControlWriter(
				socket.getOutputStream());
		final RemoteControlReader reader = new RemoteControlReader(
				socket.getInputStream());
		reader.setRemoteCommandVisitor(new IRemoteCommandVisitor() {
			public void visitDumpCommand(boolean dump, boolean reset)
					throws IOException {
				dumpRequested = dump;
				resetRequested = reset;
				if (dump) {
					writer.visitSessionInfo(new SessionInfo("TestId", 100, 200));
				}
				writer.sendCmdOk();
			}
		});
		reader.read();
	}
}
