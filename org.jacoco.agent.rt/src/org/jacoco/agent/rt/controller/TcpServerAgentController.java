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

import java.io.IOException;

import org.jacoco.core.runtime.AgentOptions;

public class TcpServerAgentController extends AbstractAgentController {
	private static final long SHUTDOWN_TIMEOUT = 5000L;
	private Thread listenerThread;
	private TcpServerListener listener;

	@Override
	public void startup() {
		AgentOptions options = getOptions();
		listener = new TcpServerListener(options.getAddress(),
				options.getPort());
		listenerThread = new Thread(listener, "JaCoCo Remote Control Listener");
		listenerThread.setDaemon(true); // Allow VM to shutdown even if this
										// thread is still running

		listenerThread.start();
	}

	@Override
	public void shutdown() {
		listener.requestShutdown();
		listenerThread.interrupt(); // interrupt any blocked IO

		waitForListenerShutdown();
	}

	private void waitForListenerShutdown() {
		// Try waiting for a normal shutdown, but give up after a set amount of
		// time. The listener thread is a daemon, so won't prevent the VM from
		// terminating
		try {
			listenerThread.join(SHUTDOWN_TIMEOUT);
		} catch (InterruptedException e) {
		}
	}

	public void writeExecutionData() throws IOException {
		// TODO need to call back into the listener to write execution data
		// This could be done by 'faking' a remote request to export coverage
		// data
	}
}
