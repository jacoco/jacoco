/*******************************************************************************
/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    Marc R. Hoffmann - migration to mock socket
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import org.jacoco.agent.rt.internal.ExceptionRecorder;
import org.jacoco.agent.rt.internal.output.IAgentOutput;
import org.jacoco.agent.rt.internal.output.TcpClientOutput;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link TcpClientOutput}.
 */
public class TcpClientOutputTest {

	private ExceptionRecorder logger;

	private IAgentOutput controller;

	private Socket remoteSocket;

	private RemoteControlWriter remoteWriter;

	private RemoteControlReader remoteReader;

	private RuntimeData data;

	@Before
	public void setup() throws Exception {
		logger = new ExceptionRecorder();
		final MockSocketConnection con = new MockSocketConnection();
		remoteSocket = con.getSocketB();
		remoteWriter = new RemoteControlWriter(remoteSocket.getOutputStream());
		controller = new TcpClientOutput(logger) {
			@Override
			protected Socket createSocket(AgentOptions options)
					throws IOException {
				return con.getSocketA();
			}
		};
		data = new RuntimeData();
		controller.startup(new AgentOptions(), data);
		remoteReader = new RemoteControlReader(remoteSocket.getInputStream());
	}

	@Test
	public void testShutdown() throws Exception {
		controller.shutdown();
		assertFalse(remoteReader.read());
		logger.assertNoException();
	}

	@Test
	public void testRemoteClose() throws Exception {
		remoteSocket.close();
		controller.shutdown();
		logger.assertNoException();
	}

	@Test
	public void testInvalidCommand() throws Exception {
		remoteWriter.visitSessionInfo(new SessionInfo("info", 1, 2));
		while (remoteReader.read()) {
		}
		controller.shutdown();
		logger.assertException(IOException.class, "No session info visitor.");
	}

	@Test
	public void testWriteExecutionData() throws Exception {
		data.getExecutionData(Long.valueOf(0x12345678), "Foo", 42);
		data.setSessionId("stubid");

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

}
