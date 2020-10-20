/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
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
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import org.jacoco.agent.rt.internal.IExceptionLogger;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Output that connects to a TCP port. This controller uses the following agent
 * options:
 * <ul>
 * <li>address</li>
 * <li>port</li>
 * </ul>
 */
public class TcpClientOutput implements IAgentOutput {
	private final IExceptionLogger logger;

	private TcpConnection connection;

	private AgentOptions options;
	private RuntimeData data;

	private Timer timer;

	/**
	 * New controller instance.
	 *
	 * @param logger
	 *            logger to use in case of exceptions is spawned threads
	 */
	public TcpClientOutput(final IExceptionLogger logger) {
		this.logger = logger;
	}

	public void startup(final AgentOptions options, final RuntimeData data)
			throws IOException {
		this.options = options;
		this.data = data;
		timer = new Timer(getClass().getName(), true);
		scheduleConnection(0);
	}

	private void scheduleConnection(final int delayMs) {
		if (connection != null) {
			try {
				connection.close();
			} catch (final IOException ioe) {
			}
		}
		if (timer != null) {
			final TimerTask task = new TimerTask() {
				@Override
				public void run() {
					try {
						final Socket socket = createSocket(options);
						connection = new TcpConnection(socket, data);
						connection.init();
						connection.run();
					} catch (final IOException e) {
						if (options.reconnectEnabled()) {
							// scheduling reconnect
							scheduleConnection(options.getReconnectMs());
						} else {
							logger.logExeption(e);
						}
					}
				}
			};
			timer.schedule(task, delayMs);
		}
	}

	public void shutdown() throws Exception {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (connection != null) {
			connection.close();
		}
	}

	public void writeExecutionData(final boolean reset) throws IOException {
		if (connection != null) {
			connection.writeExecutionData(reset);
		} else {
			throw new IOException("Writer not connected");
		}
	}

	/**
	 * Open a socket based on the given configuration.
	 *
	 * @param options
	 *            address and port configuration
	 * @return opened socket
	 * @throws IOException
	 */
	protected Socket createSocket(final AgentOptions options)
			throws IOException {
		return new Socket(options.getAddress(), options.getPort());
	}

}
