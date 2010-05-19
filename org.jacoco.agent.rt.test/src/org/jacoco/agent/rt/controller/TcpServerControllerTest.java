/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.agent.rt.controller;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.jacoco.agent.rt.ExceptionRecorder;
import org.jacoco.agent.rt.StubRuntime;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.junit.Test;

/**
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class TcpServerControllerTest {

	@Test
	public void testShutdownWithNoAccept() throws Exception {

		AgentOptions options = new AgentOptions();
		options.setPort(6301);

		IRuntime runtime = new StubRuntime();

		assertPortNotInUse(options.getPort());
		TcpServerController c1 = new TcpServerController(
				ExceptionRecorder.IGNORE_ALL);
		c1.startup(options, runtime);

		// wait for socket accept
		Thread.sleep(1000L);
		c1.shutdown();

		assertPortNotInUse(options.getPort());
	}

	@Test(expected = BindException.class)
	public void testPortAlreadyInUse() throws Exception {
		AgentOptions options = new AgentOptions();
		options.setPort(6302);

		IRuntime runtime = new StubRuntime();

		ServerSocket serverSocket = openLocalPort(options.getPort());
		try {
			assertPortInUse(options.getPort());
			TcpServerController c1 = new TcpServerController(
					ExceptionRecorder.IGNORE_ALL);
			c1.startup(options, runtime);
		} finally {
			serverSocket.close();
		}
	}

	private void assertPortNotInUse(int port) throws Exception {
		if (isPortInUse(port)) {
			fail(String.format("Port %d is in use", Integer.valueOf(port)));
		}
	}

	private void assertPortInUse(int port) throws Exception {
		if (!isPortInUse(port)) {
			fail(String.format("Port %d is not in use", Integer.valueOf(port)));
		}
	}

	private boolean isPortInUse(int port) throws IOException {
		try {
			ServerSocket serverSocket = openLocalPort(port);
			serverSocket.close();
			return false;
		} catch (BindException e) {
			return true;
		}
	}

	private ServerSocket openLocalPort(int port) throws IOException {
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress("localhost", port));

		return serverSocket;
	}
}
