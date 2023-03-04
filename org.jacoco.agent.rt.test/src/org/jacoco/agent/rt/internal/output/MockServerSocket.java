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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

/**
 * Emulation of a {@link ServerSocket} for testing purposes without any physical
 * tcp/ip connections.
 */
public class MockServerSocket extends ServerSocket {

	private final Object lock = new Object();

	private boolean closed;

	private Socket connection;

	private boolean inAccept;

	public MockServerSocket() throws IOException {
		super();
		closed = false;
		inAccept = false;
	}

	/**
	 * Establishes a new mock connection. This method blocks until the other end
	 * of the connection has been accepted.
	 *
	 * @return remote end of the mock connection
	 */
	public Socket connect() throws Exception {
		synchronized (lock) {
			final MockSocketConnection c = new MockSocketConnection();
			connection = c.getSocketA();
			lock.notifyAll();
			while (connection != null) {
				lock.wait();
			}
			return c.getSocketB();
		}
	}

	/**
	 * Blocks until another thread calls the {@link #accept()} method.
	 */
	public void waitForAccept() throws Exception {
		synchronized (lock) {
			while (!inAccept) {
				lock.wait();
			}
		}

	}

	@Override
	public void close() throws IOException {
		synchronized (lock) {
			closed = true;
			lock.notifyAll();
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public Socket accept() throws IOException {
		synchronized (lock) {
			inAccept = true;
			lock.notifyAll();
			try {
				while (connection == null) {
					if (closed) {
						throw new SocketException("socket closed");
					}
					lock.wait();
				}
				return connection;
			} catch (InterruptedException e) {
				throw new InterruptedIOException();
			} finally {
				connection = null;
				inAccept = false;
				lock.notifyAll();
			}
		}
	}

	// unsupported server socket methods:

	@Override
	public void bind(SocketAddress endpoint, int backlog) throws IOException {
		throw new AssertionError();
	}

	@Override
	public void bind(SocketAddress endpoint) throws IOException {
		throw new AssertionError();
	}

	@Override
	public ServerSocketChannel getChannel() {
		throw new AssertionError();
	}

	@Override
	public InetAddress getInetAddress() {
		throw new AssertionError();
	}

	@Override
	public int getLocalPort() {
		throw new AssertionError();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		throw new AssertionError();
	}

	@Override
	public synchronized int getReceiveBufferSize() throws SocketException {
		throw new AssertionError();
	}

	@Override
	public boolean getReuseAddress() throws SocketException {
		throw new AssertionError();
	}

	@Override
	public synchronized int getSoTimeout() throws IOException {
		throw new AssertionError();
	}

	@Override
	public boolean isBound() {
		throw new AssertionError();
	}

	@Override
	public void setPerformancePreferences(int connectionTime, int latency,
			int bandwidth) {
		throw new AssertionError();
	}

	@Override
	public synchronized void setReceiveBufferSize(int size)
			throws SocketException {
		throw new AssertionError();
	}

	@Override
	public void setReuseAddress(boolean on) throws SocketException {
		throw new AssertionError();
	}

	@Override
	public synchronized void setSoTimeout(int timeout) throws SocketException {
		throw new AssertionError();
	}

	@Override
	public String toString() {
		throw new AssertionError();
	}

}
