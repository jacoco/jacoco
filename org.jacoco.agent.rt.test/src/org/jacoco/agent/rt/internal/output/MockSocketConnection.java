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
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simulates two connected {@link Socket} objects. No physical connection is
 * established for this. The behavior includes the (inconsistent) exception
 * messages have been derived from Sun JDK 1.5.0_18-b02.
 */
public class MockSocketConnection {

	private final MockSocket socketA;

	private final MockSocket socketB;

	public MockSocketConnection() throws SocketException {
		socketA = new MockSocket();
		socketB = new MockSocket();
		socketA.connect(socketB);
	}

	public MockSocket getSocketA() {
		return socketA;
	}

	public MockSocket getSocketB() {
		return socketB;
	}

	class MockSocket extends Socket {

		private MockSocket other;

		private boolean closed;

		private final Queue<Byte> buffer = new ConcurrentLinkedQueue<Byte>();

		private final OutputStream out = new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				if (closed) {
					throw new SocketException("Socket closed");
				}
				synchronized (other.buffer) {
					other.buffer.add(Byte.valueOf((byte) b));
					other.buffer.notifyAll();
				}
			}
		};

		private final InputStream in = new InputStream() {

			@Override
			public int read() throws IOException {
				synchronized (buffer) {
					try {
						while (true) {
							if (closed) {
								throw new SocketException("socket closed");
							}
							if (other.closed) {
								return -1;
							}
							final Byte b = buffer.poll();
							buffer.notifyAll();
							if (b != null) {
								return 0xff & b.intValue();
							}
							buffer.wait();
						}
					} catch (InterruptedException e) {
						throw new InterruptedIOException();
					}
				}
			}

			@Override
			public int available() throws IOException {
				synchronized (buffer) {
					return buffer.size();
				}
			}

		};

		private MockSocket() throws SocketException {
			super((SocketImpl) null);
			closed = false;
		}

		private void connect(MockSocket other) {
			this.other = other;
			other.other = this;
		}

		public void waitUntilInputBufferIsEmpty() throws InterruptedException {
			synchronized (buffer) {
				while (!closed && !buffer.isEmpty()) {
					buffer.wait();
				}
			}
		}

		// socket methods with mocking behavior:

		@Override
		public OutputStream getOutputStream() throws IOException {
			if (isClosed()) {
				throw new SocketException("Socket is closed");
			}
			return out;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (isClosed()) {
				throw new SocketException("Socket is closed");
			}
			return in;
		}

		@Override
		public void close() throws IOException {
			synchronized (buffer) {
				closed = true;
				buffer.notifyAll();
			}
			synchronized (other.buffer) {
				other.buffer.notifyAll();
			}
		}

		@Override
		public boolean isClosed() {
			synchronized (buffer) {
				return closed;
			}
		}

		// unsupported socket methods:

		@Override
		public void bind(SocketAddress bindpoint) throws IOException {
			throw new AssertionError();
		}

		@Override
		public void connect(SocketAddress endpoint, int timeout)
				throws IOException {
			throw new AssertionError();
		}

		@Override
		public void connect(SocketAddress endpoint) throws IOException {
			throw new AssertionError();
		}

		@Override
		public SocketChannel getChannel() {
			throw new AssertionError();
		}

		@Override
		public InetAddress getInetAddress() {
			throw new AssertionError();
		}

		@Override
		public boolean getKeepAlive() throws SocketException {
			throw new AssertionError();
		}

		@Override
		public InetAddress getLocalAddress() {
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
		public boolean getOOBInline() throws SocketException {
			throw new AssertionError();
		}

		@Override
		public int getPort() {
			throw new AssertionError();
		}

		@Override
		public synchronized int getReceiveBufferSize() throws SocketException {
			throw new AssertionError();
		}

		@Override
		public SocketAddress getRemoteSocketAddress() {
			throw new AssertionError();
		}

		@Override
		public boolean getReuseAddress() throws SocketException {
			throw new AssertionError();
		}

		@Override
		public synchronized int getSendBufferSize() throws SocketException {
			throw new AssertionError();
		}

		@Override
		public int getSoLinger() throws SocketException {
			throw new AssertionError();
		}

		@Override
		public synchronized int getSoTimeout() throws SocketException {
			throw new AssertionError();
		}

		@Override
		public boolean getTcpNoDelay() throws SocketException {
			throw new AssertionError();
		}

		@Override
		public int getTrafficClass() throws SocketException {
			throw new AssertionError();
		}

		@Override
		public boolean isBound() {
			throw new AssertionError();
		}

		@Override
		public boolean isConnected() {
			throw new AssertionError();
		}

		@Override
		public boolean isInputShutdown() {
			throw new AssertionError();
		}

		@Override
		public boolean isOutputShutdown() {
			throw new AssertionError();
		}

		@Override
		public void sendUrgentData(int data) throws IOException {
			throw new AssertionError();
		}

		@Override
		public void setKeepAlive(boolean on) throws SocketException {
			throw new AssertionError();
		}

		@Override
		public void setOOBInline(boolean on) throws SocketException {
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
		public synchronized void setSendBufferSize(int size)
				throws SocketException {
			throw new AssertionError();
		}

		@Override
		public void setSoLinger(boolean on, int linger) throws SocketException {
			throw new AssertionError();
		}

		@Override
		public synchronized void setSoTimeout(int timeout)
				throws SocketException {
			throw new AssertionError();
		}

		@Override
		public void setTcpNoDelay(boolean on) throws SocketException {
			throw new AssertionError();
		}

		@Override
		public void setTrafficClass(int tc) throws SocketException {
			throw new AssertionError();
		}

		@Override
		public void shutdownInput() throws IOException {
			throw new AssertionError();
		}

		@Override
		public void shutdownOutput() throws IOException {
			throw new AssertionError();
		}

	}

}
