/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jacoco.agent.rt.controller.IAgentController;
import org.jacoco.agent.rt.controller.LocalController;
import org.jacoco.agent.rt.controller.MBeanController;
import org.jacoco.agent.rt.controller.TcpClientController;
import org.jacoco.agent.rt.controller.TcpServerController;
import org.jacoco.core.runtime.AbstractRuntime;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;
import org.jacoco.core.runtime.RuntimeData;

/**
 * The agent manages the life cycle of JaCoCo runtime.
 */
public class Agent {

	private static Agent singleton;

	/**
	 * Returns a global instance which is already started. If the method is
	 * called the first time the instance is created with the given options.
	 * 
	 * @param options
	 *            options to configure the instance
	 * @return global instance
	 */
	public static synchronized Agent getInstance(final AgentOptions options) {
		if (singleton == null) {
			final Agent agent = new Agent(options, IExceptionLogger.SYSTEM_ERR);
			agent.startup();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					agent.shutdown();
				}
			});
			singleton = agent;
		}
		return singleton;
	}

	private final AgentOptions options;

	private final IExceptionLogger logger;

	private IAgentController controller;

	private final RuntimeData data;

	/**
	 * Creates a new agent with the given agent options.
	 * 
	 * @param options
	 *            agent options
	 * @param logger
	 *            logger used by this agent
	 */
	public Agent(final AgentOptions options, final IExceptionLogger logger) {
		this.options = options;
		this.logger = logger;
		this.data = new RuntimeData();
	}

	/**
	 * Returns the runtime data object created by this agent
	 * 
	 * @return runtime data for this agent instance
	 */
	public RuntimeData getData() {
		return data;
	}

	/**
	 * Initializes this agent.
	 * 
	 */
	public void startup() {
		try {
			String sessionId = options.getSessionId();
			if (sessionId == null) {
				sessionId = createSessionId();
			}
			data.setSessionId(sessionId);
			controller = createAgentController();
			controller.startup(options, data);
		} catch (final Exception e) {
			logger.logExeption(e);
		}
	}

	/**
	 * Shutdown the agent again.
	 */
	public void shutdown() {
		try {
			if (options.getDumpOnExit()) {
				controller.writeExecutionData();
			}
			controller.shutdown();
		} catch (final Exception e) {
			logger.logExeption(e);
		}
	}

	/**
	 * Create controller implementation as given by the agent options.
	 * 
	 * @return configured controller implementation
	 */
	IAgentController createAgentController() {
		final OutputMode controllerType = options.getOutput();
		switch (controllerType) {
		case file:
			return new LocalController();
		case tcpserver:
			return new TcpServerController(logger);
		case tcpclient:
			return new TcpClientController(logger);
		case mbean:
			return new MBeanController();
		default:
			throw new AssertionError(controllerType);
		}
	}

	private String createSessionId() {
		String host;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			host = "unknownhost";
		}
		return host + "-" + AbstractRuntime.createRandomId();
	}

}
