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
import java.net.Socket;

import org.jacoco.agent.rt.IExceptionLogger;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;

/**
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class TcpServerController implements IAgentController {

	private boolean active;

	private TcpConnection connection;

	private final IExceptionLogger logger;

	private ServerSocket serverSocket;

	private Thread worker;

	public TcpServerController(final IExceptionLogger logger) {
		this.logger = logger;
	}

	public void startup(final AgentOptions options, final IRuntime runtime)
			throws IOException {
		active = true;
		serverSocket = new ServerSocket(options.getPort(), 1, InetAddress
				.getByName(options.getAddress()));
		worker = new Thread(new Runnable() {
			public void run() {
				while (active) {
					try {
						final Socket socket = serverSocket.accept();
						connection = new TcpConnection(socket, runtime);
						connection.run();
					} catch (IOException e) {
						logger.logExeption(e);
					}
				}
			}
		});
		worker.setName(getClass().getName());
		worker.setDaemon(true);
		worker.start();
	}

	public void shutdown() throws Exception {
		active = false;
		if (connection != null) {
			connection.close();
		}
		serverSocket.close();
		worker.join();
	}

	public void writeExecutionData() throws IOException {
		if (connection != null) {
			connection.writeExecutionData();
		}
	}

}
