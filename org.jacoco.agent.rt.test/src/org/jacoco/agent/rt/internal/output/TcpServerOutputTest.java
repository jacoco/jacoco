/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    Marc R. Hoffmann - migration to mock socket
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.jacoco.agent.rt.internal.ExceptionRecorder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link TcpServerOutput}.
 */
public class TcpServerOutputTest {

	private ExceptionRecorder logger;

	private AgentOptions options;

	private MockServerSocket serverSocket;

	private TcpServerOutput controller;

	private RuntimeData data;

	@Before
	public void setup() throws Exception {
		options = new AgentOptions();
		logger = new ExceptionRecorder();
		serverSocket = new MockServerSocket();
		controller = new TcpServerOutput(logger) {
			@Override
			protected ServerSocket createServerSocket(AgentOptions options)
					throws IOException {
				return serverSocket;
			}
		};
		data = new RuntimeData();
		controller.startup(options, data);
	}

	@Test
	public void testShutdownWithoutConnection() throws Exception {
		serverSocket.waitForAccept();
		controller.shutdown();
		logger.assertNoException();
	}

	@Test
	public void testShutdownWithConnection() throws Exception {
		serverSocket.waitForAccept();
		new ExecutionDataWriter(serverSocket.connect().getOutputStream());
		controller.shutdown();
		logger.assertNoException();
	}

	@Test
	public void testWriteExecutionData() throws Exception {
		data.getExecutionData(Long.valueOf(0x12345678), "Foo", 42)
				.getProbes()[0] = true;
		data.setSessionId("stubid");

		final Socket socket = serverSocket.connect();
		final RemoteControlWriter remoteWriter = new RemoteControlWriter(
				socket.getOutputStream());
		final RemoteControlReader remoteReader = new RemoteControlReader(
				socket.getInputStream());

		// First process a NOP command to ensure the connection is initialized:
		remoteWriter.visitDumpCommand(false, false);
		remoteReader.read();

		// Now the actual test starts:
		controller.writeExecutionData(false);

		final ExecutionDataStore execStore = new ExecutionDataStore();
		remoteReader.setExecutionDataVisitor(execStore);
		final SessionInfoStore infoStore = new SessionInfoStore();
		remoteReader.setSessionInfoVisitor(infoStore);
		remoteReader.read();

		assertEquals("Foo", execStore.get(0x12345678).getName());

		final List<SessionInfo> infos = infoStore.getInfos();
		assertEquals(1, infos.size());
		assertEquals("stubid", infos.get(0).getId());

		logger.assertNoException();
		controller.shutdown();
	}

	@Test
	public void testInvalidHeader() throws Exception {
		final Socket socket = serverSocket.connect();
		final OutputStream out = socket.getOutputStream();
		out.write(0xca);
		out.write(0xfe);
		out.write(0xba);
		out.write(0xbe);
		serverSocket.waitForAccept();
		logger.assertException(IOException.class,
				"Invalid execution data file.");
		controller.shutdown();
	}

	@Test
	public void testGetInetAddressLoopback() throws UnknownHostException {
		final InetAddress addr = controller.getInetAddress(null);
		assertTrue(addr.isLoopbackAddress());
	}

	@Test
	public void testGetInetAddressAny() throws UnknownHostException {
		final InetAddress addr = controller.getInetAddress("*");
		assertNull(addr);
	}

}
