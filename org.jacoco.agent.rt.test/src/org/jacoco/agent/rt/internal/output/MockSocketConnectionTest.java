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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link MockSocketConnection}.
 */
public class MockSocketConnectionTest extends ExecutorTestBase {

	/**
	 * To verify that the tests reflect the behavior of real TCP sockets this
	 * flag can be set to <code>true</code>.
	 */
	private static final boolean REAL_SOCKETS = Boolean
			.getBoolean("MockSocketConnectionTest.realSockets");

	private Socket a;

	private Socket b;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		if (REAL_SOCKETS) {
			createRealSockets();
		} else {
			createMockSockets();
		}
	}

	private void createMockSockets() throws Exception {
		final MockSocketConnection con = new MockSocketConnection();
		a = con.getSocketA();
		b = con.getSocketB();
	}

	private void createRealSockets() throws Exception {
		System.err.println("Using Real Sockets!");
		final InetAddress addr = InetAddress.getByName("localhost");
		ServerSocket ssocket = new ServerSocket(16300, 1, addr);
		a = new Socket(addr, 16300);
		b = ssocket.accept();
		ssocket.close();
	}

	@Test
	public void testIsClosed() throws Exception {
		assertFalse(a.isClosed());
		a.close();
		assertTrue(a.isClosed());
	}

	@Test(expected = SocketException.class)
	public void testGetInputStreamOnClosedSocket() throws Exception {
		a.close();
		a.getInputStream();
	}

	@Test(expected = SocketException.class)
	public void testReadOnClosedSocket() throws Exception {
		final InputStream in = a.getInputStream();
		a.close();
		in.read();
	}

	@Test(expected = SocketException.class)
	public void testReadOnClosedSocketAsync() throws Throwable {
		final InputStream in = a.getInputStream();
		final Future<Void> f = executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				in.read();
				return null;
			}
		});

		assertBlocks(f);

		a.close();
		try {
			f.get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

	@Test(expected = SocketException.class)
	public void testGetOutputStreamOnClosedSocket() throws Exception {
		a.close();
		a.getOutputStream();
	}

	@Test(expected = SocketException.class)
	public void testWriteOnClosedSocket() throws Exception {
		final OutputStream out = a.getOutputStream();
		a.close();
		out.write(123);
	}

	@Test
	public void testContents() throws Exception {
		final InputStream in = a.getInputStream();
		final OutputStream out = b.getOutputStream();

		assertEquals(0, in.available());

		out.write(0);
		out.write(1);
		out.write(2);
		out.write(255);

		if (!REAL_SOCKETS) {
			// Real sockets will have a delay
			assertEquals(4, in.available());
		}

		assertEquals(0, in.read());
		assertEquals(1, in.read());
		assertEquals(2, in.read());
		assertEquals(255, in.read());
	}

	@Test
	public void testWaitForContents() throws Exception {
		final InputStream in = a.getInputStream();
		final OutputStream out = b.getOutputStream();

		final Future<Byte> f = executor.submit(new Callable<Byte>() {
			public Byte call() throws Exception {
				return Byte.valueOf((byte) in.read());
			}
		});

		assertBlocks(f);
		out.write(123);
		assertEquals(123, f.get().byteValue());
	}

	@Test
	public void testCloseOtherSocket() throws Exception {
		b.close();
		final InputStream in = a.getInputStream();
		assertEquals(-1, in.read());
	}

	@Test
	public void testCloseOtherSocketAsync() throws Exception {
		final InputStream in = a.getInputStream();

		final Future<Byte> f = executor.submit(new Callable<Byte>() {
			public Byte call() throws Exception {
				return Byte.valueOf((byte) in.read());
			}
		});

		assertBlocks(f);
		b.close();

		assertEquals(-1, f.get().byteValue());
	}

}
