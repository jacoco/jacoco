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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.jacoco.agent.rt.IExceptionLogger;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;

/**
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class TcpServerController implements IAgentController {

	private TcpConnection connection;

	private final IExceptionLogger logger;

	private ServerSocket serverSocket;

	private Thread worker;

	public TcpServerController(final IExceptionLogger logger) {
		this.logger = logger;
	}

	public void startup(final AgentOptions options, final IRuntime runtime)
			throws IOException {
		serverSocket = createServerSocket(options);
		worker = new Thread(new Runnable() {
			public void run() {
				while (!serverSocket.isClosed()) {
					try {
						connection = new TcpConnection(serverSocket.accept(),
								runtime);
						connection.init();
						connection.run();
					} catch (IOException e) {
						// If the serverSocket is closed while accepting
						// connections a SocketException is expected.
						if (!serverSocket.isClosed()) {
							logger.logExeption(e);
						}
					}
				}
			}
		});
		worker.setName(getClass().getName());
		worker.setDaemon(true);
		worker.start();
	}

	public void shutdown() throws Exception {
		serverSocket.close();
		if (connection != null) {
			connection.close();
		}
		worker.join();
	}

	public void writeExecutionData() throws IOException {
		if (connection != null) {
			connection.writeExecutionData();
		}
	}

	/**
	 * Open a server socket based on the given configuration.
	 * 
	 * @param options
	 *            address and port configuration
	 * @return opened server socket
	 * @throws IOException
	 */
	protected ServerSocket createServerSocket(final AgentOptions options)
			throws IOException {
		final InetAddress addr = InetAddress.getByName(options.getAddress());
		return new ServerSocket(options.getPort(), 1, addr);
	}

}
