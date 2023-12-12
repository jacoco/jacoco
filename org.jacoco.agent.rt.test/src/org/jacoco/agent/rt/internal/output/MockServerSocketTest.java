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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link MockServerSocket}.
 */
public class MockServerSocketTest extends ExecutorTestBase {

	private ServerSocket serverSocket;

	/**
	 * To verify that the tests reflect the behavior of real TCP sockets this
	 * flag can be set to <code>true</code>.
	 */
	private static final boolean REAL_SOCKETS = Boolean
			.getBoolean("MockServerSocketTest.realSockets");

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		if (REAL_SOCKETS) {
			System.err.println("Using Real Sockets!");
			final InetAddress addr = InetAddress.getByName("localhost");
			serverSocket = new ServerSocket(16300, 1, addr);
		} else {
			serverSocket = new MockServerSocket();
		}
	}

	@After
	@Override
	public void teardown() throws Exception {
		serverSocket.close();
		super.teardown();
	}

	private Socket connect() throws Exception {
		if (REAL_SOCKETS) {
			final InetAddress addr = InetAddress.getByName("localhost");
			return new Socket(addr, 16300);
		} else {
			return ((MockServerSocket) serverSocket).connect();
		}

	}

	@Test
	public void testClose() throws Exception {
		assertFalse(serverSocket.isClosed());
		serverSocket.close();
		assertTrue(serverSocket.isClosed());
	}

	@Test(expected = SocketException.class)
	public void testCloseWhileAccept() throws Throwable {
		final Future<Socket> f = executor.submit(new Callable<Socket>() {
			public Socket call() throws Exception {
				return serverSocket.accept();
			}
		});
		assertBlocks(f);
		serverSocket.close();
		try {
			f.get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

	@Test
	public void testAccept() throws Exception {
		final Future<Socket> f = executor.submit(new Callable<Socket>() {
			public Socket call() throws Exception {
				return serverSocket.accept();
			}
		});
		assertBlocks(f);
		connect().getOutputStream().write(123);
		final Socket socket = f.get();
		assertNotNull(socket);
		assertEquals(123, socket.getInputStream().read());
	}

	@Test(expected = SocketException.class)
	public void testAcceptOnClosedServerSocket() throws Exception {
		serverSocket.close();
		serverSocket.accept();
	}

	@Test
	public void testConnect() throws Exception {
		if (!REAL_SOCKETS) {
			final Future<Socket> f = executor.submit(new Callable<Socket>() {
				public Socket call() throws Exception {
					return ((MockServerSocket) serverSocket).connect();
				}
			});
			assertBlocks(f);
			serverSocket.accept().getOutputStream().write(42);
			final Socket socket = f.get();
			assertNotNull(socket);
			assertEquals(42, socket.getInputStream().read());
		}
	}

	@Test
	public void testWaitForAccept() throws Exception {
		if (!REAL_SOCKETS) {
			final Future<Void> f = executor.submit(new Callable<Void>() {
				public Void call() throws Exception {
					((MockServerSocket) serverSocket).waitForAccept();
					connect();
					return null;
				}
			});
			assertBlocks(f);
			serverSocket.accept();
			f.get();
		}
	}

}
