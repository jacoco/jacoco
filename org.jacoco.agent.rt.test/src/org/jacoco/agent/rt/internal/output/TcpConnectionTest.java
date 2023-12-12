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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link TcpConnection}.
 */
public class TcpConnectionTest extends ExecutorTestBase {

	private MockSocketConnection mockConnection;

	private RuntimeData data;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		mockConnection = new MockSocketConnection();
		data = new RuntimeData();
	}

	@Test(expected = IOException.class)
	public void testInvalidHeader() throws Exception {
		final OutputStream remoteOut = mockConnection.getSocketB()
				.getOutputStream();
		remoteOut.write(0x01);
		remoteOut.write(0xC0);
		remoteOut.write(0xCA);
		final TcpConnection connection = new TcpConnection(
				mockConnection.getSocketA(), data);
		connection.init();
		connection.run();
	}

	@Test(expected = IOException.class)
	public void testInvalidContent() throws Exception {
		final OutputStream remoteOut = mockConnection.getSocketB()
				.getOutputStream();
		new ExecutionDataWriter(remoteOut);
		final TcpConnection con = new TcpConnection(mockConnection.getSocketA(),
				data);
		con.init();
		remoteOut.write(123);
		con.run();
	}

	/**
	 * Remote endpoint is closed after a valid header has been send.
	 */
	@Test
	public void testRemoteClose() throws Exception {
		final OutputStream remoteOut = mockConnection.getSocketB()
				.getOutputStream();
		new ExecutionDataWriter(remoteOut);

		final TcpConnection con = new TcpConnection(mockConnection.getSocketA(),
				data);
		con.init();

		final Future<Void> f = executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				con.run();
				return null;
			}
		});

		assertBlocks(f);

		mockConnection.getSocketA().waitUntilInputBufferIsEmpty();
		mockConnection.getSocketB().close();
		f.get();
	}

	/**
	 * Local socket is closed while waiting for commands.
	 *
	 * @throws Exception
	 */
	@Test
	public void testLocalClose() throws Exception {
		final OutputStream remoteOut = mockConnection.getSocketB()
				.getOutputStream();
		new ExecutionDataWriter(remoteOut);

		final TcpConnection con = new TcpConnection(mockConnection.getSocketA(),
				data);
		con.init();

		final Future<Void> f = executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				con.run();
				return null;
			}
		});

		assertBlocks(f);

		con.close();
		f.get();
	}

	@Test
	public void testRemoteDump() throws Exception {
		data.getExecutionData(Long.valueOf(0x12345678), "Foo", 42)
				.getProbes()[0] = true;
		data.setSessionId("stubid");

		final RemoteControlWriter remoteWriter = new RemoteControlWriter(
				mockConnection.getSocketB().getOutputStream());

		final TcpConnection con = new TcpConnection(mockConnection.getSocketA(),
				data);
		con.init();

		final Future<Void> f = executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				con.run();
				return null;
			}
		});

		assertBlocks(f);

		remoteWriter.visitDumpCommand(true, false);
		readAndAssertData();

		con.close();
		f.get();
	}

	@Test
	public void testLocalDump() throws Exception {
		data.getExecutionData(Long.valueOf(0x12345678), "Foo", 42)
				.getProbes()[0] = true;
		data.setSessionId("stubid");

		new RemoteControlWriter(mockConnection.getSocketB().getOutputStream());

		final TcpConnection con = new TcpConnection(mockConnection.getSocketA(),
				data);
		con.init();

		final Future<Void> f = executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				con.run();
				return null;
			}
		});

		assertBlocks(f);

		con.writeExecutionData(false);
		readAndAssertData();

		con.close();
		f.get();
	}

	@Test
	public void testLocalDumpWithoutInit() throws Exception {
		final TcpConnection con = new TcpConnection(mockConnection.getSocketA(),
				data);
		// Must not write any data as we're not initialized:
		con.writeExecutionData(false);

		assertEquals(0,
				mockConnection.getSocketB().getInputStream().available());
	}

	private void readAndAssertData() throws IOException {
		final RemoteControlReader remoteReader = new RemoteControlReader(
				mockConnection.getSocketB().getInputStream());

		final ExecutionDataStore execStore = new ExecutionDataStore();
		remoteReader.setExecutionDataVisitor(execStore);
		final SessionInfoStore infoStore = new SessionInfoStore();
		remoteReader.setSessionInfoVisitor(infoStore);

		assertTrue(remoteReader.read());

		final List<SessionInfo> infos = infoStore.getInfos();
		assertEquals(1, infos.size());
		assertEquals("stubid", infos.get(0).getId());

		assertEquals("Foo", execStore.get(0x12345678).getName());
	}

	@Test
	public void testRemoteReset() throws Exception {
		data.getExecutionData(Long.valueOf(123), "Foo", 1)
				.getProbes()[0] = true;

		final RemoteControlWriter remoteWriter = new RemoteControlWriter(
				mockConnection.getSocketB().getOutputStream());

		final TcpConnection con = new TcpConnection(mockConnection.getSocketA(),
				data);
		con.init();

		final Future<Void> f = executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				con.run();
				return null;
			}
		});

		assertBlocks(f);

		remoteWriter.visitDumpCommand(false, true);

		final RemoteControlReader remoteReader = new RemoteControlReader(
				mockConnection.getSocketB().getInputStream());

		final ExecutionDataStore execStore = new ExecutionDataStore();
		remoteReader.setExecutionDataVisitor(execStore);
		final SessionInfoStore infoStore = new SessionInfoStore();
		remoteReader.setSessionInfoVisitor(infoStore);

		assertTrue(remoteReader.read());
		assertTrue(infoStore.getInfos().isEmpty());
		assertTrue(execStore.getContents().isEmpty());
		assertFalse(data.getExecutionData(Long.valueOf(123), "Foo", 1)
				.getProbes()[0]);

		con.close();
		f.get();
	}

}
