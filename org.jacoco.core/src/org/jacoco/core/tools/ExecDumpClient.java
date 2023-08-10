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
package org.jacoco.core.tools;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;

import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * A client for remote execution data dumps.
 */
public class ExecDumpClient {

	private boolean dump;
	private boolean reset;
	private int retryCount;
	private long retryDelay;

	/**
	 * New instance with the defaults <code>dump==true</code>,
	 * <code>reset==false</code>, <code>retryCount==0</code> and
	 * <code>retryDelay=1000</code>.
	 */
	public ExecDumpClient() {
		this.dump = true;
		this.reset = false;
		this.retryCount = 0;
		this.setRetryDelay(1000);
	}

	/**
	 * Specifies whether a dump should be requested
	 *
	 * @param dump
	 *            <code>true</code> if a dump should be requested
	 */
	public void setDump(final boolean dump) {
		this.dump = dump;
	}

	/**
	 * Specifies whether execution data should be reset.
	 *
	 * @param reset
	 *            <code>true</code> if execution data should be reset
	 */
	public void setReset(final boolean reset) {
		this.reset = reset;
	}

	/**
	 * Sets the number of retry attempts to connect to the target socket. This
	 * allows to wait for a certain time until the target agent has initialized.
	 *
	 * @param retryCount
	 *            number of retries
	 */
	public void setRetryCount(final int retryCount) {
		this.retryCount = retryCount;
	}

	/**
	 * Sets the delay time before between connection attempts.
	 *
	 * @param retryDelay
	 *            delay in milliseconds
	 */
	public void setRetryDelay(final long retryDelay) {
		this.retryDelay = retryDelay;
	}

	/**
	 * Requests a dump from the given end-point.
	 *
	 * @param address
	 *            IP-Address to connect to
	 * @param port
	 *            port to connect to
	 * @return container for the dumped data
	 * @throws IOException
	 *             in case the dump can not be requested
	 */
	public ExecFileLoader dump(final String address, final int port)
			throws IOException {
		return dump(InetAddress.getByName(address), port);
	}

	/**
	 * Requests a dump from the given end-point.
	 *
	 * @param address
	 *            host name or IP-Address to connect to
	 * @param port
	 *            port to connect to
	 * @return container for the dumped data
	 * @throws IOException
	 *             in case the dump can not be requested
	 */
	public ExecFileLoader dump(final InetAddress address, final int port)
			throws IOException {
		final ExecFileLoader loader = new ExecFileLoader();
		final Socket socket = tryConnect(address, port);
		try {
			final RemoteControlWriter remoteWriter = new RemoteControlWriter(
					socket.getOutputStream());
			final RemoteControlReader remoteReader = new RemoteControlReader(
					socket.getInputStream());
			remoteReader.setSessionInfoVisitor(loader.getSessionInfoStore());
			remoteReader
					.setExecutionDataVisitor(loader.getExecutionDataStore());

			remoteWriter.visitDumpCommand(dump, reset);

			if (!remoteReader.read()) {
				throw new IOException("Socket closed unexpectedly.");
			}

		} finally {
			socket.close();
		}
		return loader;
	}

	private Socket tryConnect(final InetAddress address, final int port)
			throws IOException {
		int count = 0;
		while (true) {
			try {
				onConnecting(address, port);
				return new Socket(address, port);
			} catch (final IOException e) {
				if (++count > retryCount) {
					throw e;
				}
				onConnectionFailure(e);
				sleep();
			}
		}
	}

	private void sleep() throws InterruptedIOException {
		try {
			Thread.sleep(retryDelay);
		} catch (final InterruptedException e) {
			throw new InterruptedIOException();
		}
	}

	/**
	 * This method can be overwritten to get an event just before a connection
	 * is made.
	 *
	 * @param address
	 *            target address
	 * @param port
	 *            target port
	 */
	protected void onConnecting(
			@SuppressWarnings("unused") final InetAddress address,
			@SuppressWarnings("unused") final int port) {
	}

	/**
	 * This method can be overwritten to get an event for connection failures
	 * when another retry will be attempted.
	 *
	 * @param exception
	 *            connection error
	 */
	protected void onConnectionFailure(
			@SuppressWarnings("unused") final IOException exception) {
	}

}
