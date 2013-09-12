/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Chas Honton - initial implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;

import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

class SessionPump {

	private final ServerCollector collector;
	private final Socket socket;
	private final String threadName;
	private RemoteControlWriter controller;
	private OutputStream outputStream;

	SessionPump(final ServerCollector collector, final Socket socket)
			throws IOException {
		this.collector = collector;
		this.socket = socket;
		this.threadName = socket.getRemoteSocketAddress().toString();
	}

	public void run(final ThreadGroup group, final boolean dumpOnExit) {
		new Thread(group, threadName) {
			@Override
			public void run() {
				try {
					pump(primepump());
				} catch (final ClosedByInterruptException e) {
					logDebugMessage(" interrupted");
				} catch (final IOException x) {
					logErrorMessage(" pump failed", x);
				} finally {
					cleanup();
				}
			};

			@Override
			public void interrupt() {
				try {
					if (dumpOnExit) {
						controller.visitDumpCommand(true, false);
						controller.sendCmdOk();
					}
					socket.shutdownOutput();
				} catch (final IOException x) {
					logErrorMessage(" shutdown failed", x);
				}
			}
		}.start();
	}

	private RemoteControlReader primepump() throws IOException {
		logDebugMessage(" sending header");
		outputStream = socket.getOutputStream();
		controller = new RemoteControlWriter(outputStream);

		final RemoteControlReader remoteReader = new RemoteControlReader(
				socket.getInputStream());
		remoteReader.setSessionInfoVisitor(collector);
		remoteReader.setExecutionDataVisitor(collector);

		return remoteReader;
	}

	private void pump(final RemoteControlReader remoteReader)
			throws IOException {
		while (remoteReader.read()) {
		}
		logDebugMessage(" end of data");
	}

	void cleanup() {
		try {
			socket.close();
		} catch (final IOException x) {
			logErrorMessage(" cleanup failed", x);
		}
	}

	private void logDebugMessage(final String msg) {
		collector.getLog().debug("jacoco-collector " + threadName + msg);
	}

	private void logErrorMessage(final String msg, final Exception e) {
		collector.getLog().error("jacoco-collector " + threadName + msg, e);
	}
}