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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.runtime.AgentOptions;

class ServerCollector extends AbstractCollector implements ISessionInfoVisitor,
		IExecutionDataVisitor {

	private final Log log;
	private final ThreadGroup group;
	private ExecutionDataWriter fileWriter;

	public ServerCollector(final AgentOptions options, final Log log) {
		super(options);
		this.log = log;

		group = new ThreadGroup("jacoco-collector") {
			@Override
			public void uncaughtException(final Thread t, final Throwable e) {
				log.error(t.getName(), e);
			}
		};
		group.setDaemon(true);
	}

	public void startListener() throws MojoExecutionException, IOException {
		fileWriter = new ExecutionDataWriter(createFileOutputStream());
		startAcceptorThread(createServerSocket());
	}

	private void startAcceptorThread(final ServerSocket server) {
		new Thread(group, "acceptor") {

			@Override
			public void run() {
				logDebugMessage("started");
				try {
					acceptIncoming();
				} catch (final SocketException e) {
					logDebugMessage("stopped");
				} catch (final IOException e) {
					logErrorMessage("failed", e);
				}
			}

			@Override
			public void interrupt() {
				logDebugMessage("shutting down");
				try {
					server.close();
				} catch (final IOException x) {
					logErrorMessage("shutdown failed", x);
					super.interrupt();
				}
			}

			private void acceptIncoming() throws IOException {
				while (true) {
					final Socket accept = server.accept();
					logDebugMessage("accepted "
							+ accept.getRemoteSocketAddress());
					final SessionPump pump = new SessionPump(
							ServerCollector.this, accept);
					pump.run(group, options.getDumpOnExit());
				}
			}

			private void logDebugMessage(final String msg) {
				log.debug("jacoco-collector acceptor " + msg);
			}

			private void logErrorMessage(final String msg, final Exception e) {
				log.error("jacoco-collector acceptor " + msg, e);
			}
		}.start();
	}

	ServerSocket createServerSocket() throws MojoExecutionException {
		final InetSocketAddress endpoint = getSocketAddress(false);
		try {
			final ServerSocket server = new ServerSocket();
			server.bind(endpoint);
			return server;
		} catch (final IOException e) {
			throw new MojoExecutionException("Can not open server socket "
					+ endpoint, e);
		}
	}

	void stop(final long maxWait) {
		final long endTime = System.currentTimeMillis() + maxWait;
		try {
			log.debug("jacoco-collector interrupting group");
			group.interrupt();

			log.debug("jacoco-collector waiting");
			while (group.activeCount() > 0
					&& System.currentTimeMillis() < endTime) {
				Thread.sleep(1000);
			}

		} catch (final InterruptedException e) {
			log.error(e);
		}
		try {
			synchronized (fileWriter) {
				fileWriter.flush();
			}
		} catch (final IOException x) {
			log.error(x);
		}
	}

	Log getLog() {
		return log;
	}

	// @Override
	public void visitSessionInfo(final SessionInfo info) {
		synchronized (fileWriter) {
			fileWriter.visitSessionInfo(info);
		}
	}

	// @Override
	public void visitClassExecution(final ExecutionData data) {
		synchronized (fileWriter) {
			fileWriter.visitClassExecution(data);
		}
	}
}