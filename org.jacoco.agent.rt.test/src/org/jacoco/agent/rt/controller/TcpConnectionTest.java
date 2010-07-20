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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jacoco.agent.rt.StubRuntime;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link TcpConnection}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class TcpConnectionTest extends ExecutorTestBase {

	private MockSocketConnection mockConnection;

	private StubRuntime runtime;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		mockConnection = new MockSocketConnection();
		runtime = new StubRuntime();
	}

	@Test(expected = IOException.class)
	public void testInvalidHeader() throws Exception {
		final OutputStream remoteOut = mockConnection.getSocketB()
				.getOutputStream();
		remoteOut.write(0x01);
		remoteOut.write(0xC0);
		remoteOut.write(0xCA);
		new TcpConnection(mockConnection.getSocketA(), runtime).init();
	}

	@Test(expected = IOException.class)
	public void testInvalidContent() throws Exception {
		final OutputStream remoteOut = mockConnection.getSocketB()
				.getOutputStream();
		new ExecutionDataWriter(remoteOut);
		final TcpConnection con = new TcpConnection(
				mockConnection.getSocketA(), runtime);
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

		final TcpConnection con = new TcpConnection(
				mockConnection.getSocketA(), runtime);
		con.init();

		final Future<Void> f = executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				con.run();
				return null;
			}
		});

		assertBlocks(f);

		mockConnection.getSocketB().close();
		f.get();
	}

	/**
	 * Remote endpoint is closed before even a valid header was send.
	 */
	@Test(expected = EOFException.class)
	public void testRemoteCloseWithoutHeader() throws Throwable {
		final TcpConnection con = new TcpConnection(
				mockConnection.getSocketA(), runtime);

		final Future<Void> f = executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				con.init();
				return null;
			}
		});

		assertBlocks(f);

		mockConnection.getSocketB().close();
		try {
			f.get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
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

		final TcpConnection con = new TcpConnection(
				mockConnection.getSocketA(), runtime);
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
		final RemoteControlWriter remoteWriter = new RemoteControlWriter(
				mockConnection.getSocketB().getOutputStream());

		final TcpConnection con = new TcpConnection(
				mockConnection.getSocketA(), runtime);
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
		new RemoteControlWriter(mockConnection.getSocketB().getOutputStream());

		final TcpConnection con = new TcpConnection(
				mockConnection.getSocketA(), runtime);
		con.init();

		final Future<Void> f = executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				con.run();
				return null;
			}
		});

		assertBlocks(f);

		con.writeExecutionData();
		readAndAssertData();

		con.close();
		f.get();
	}

	@Test
	public void testLocalDumpWithoutInit() throws Exception {
		final TcpConnection con = new TcpConnection(
				mockConnection.getSocketA(), runtime);
		// Must not write any data as we're not initialized:
		con.writeExecutionData();

		assertEquals(0, mockConnection.getSocketB().getInputStream()
				.available());
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
		runtime.fillProbes();

		final RemoteControlWriter remoteWriter = new RemoteControlWriter(
				mockConnection.getSocketB().getOutputStream());

		final TcpConnection con = new TcpConnection(
				mockConnection.getSocketA(), runtime);
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
		runtime.assertNoProbes();

		con.close();
		f.get();
	}
}
