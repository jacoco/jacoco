/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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

import java.lang.instrument.Instrumentation;
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
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.ModifiedSystemClassRuntime;
import org.jacoco.core.runtime.RuntimeData;

/**
 * The agent which is referred as the <code>Premain-Class</code>.
 */
public class JacocoAgent {

	private final AgentOptions options;

	private final IExceptionLogger logger;

	private IAgentController controller;

	/**
	 * Creates a new agent with the given agent options.
	 * 
	 * @param options
	 *            agent options
	 * @param logger
	 *            logger used by this agent
	 */
	public JacocoAgent(final AgentOptions options, final IExceptionLogger logger) {
		this.options = options;
		this.logger = logger;
	}

	/**
	 * Creates a new agent with the given agent options string.
	 * 
	 * @param options
	 *            agent options as text string
	 * @param logger
	 *            logger used by this agent
	 */
	public JacocoAgent(final String options, final IExceptionLogger logger) {
		this(new AgentOptions(options), logger);
	}

	/**
	 * Initializes this agent.
	 * 
	 * @param inst
	 *            instrumentation services
	 * @throws Exception
	 *             internal startup problem
	 */
	public void init(final Instrumentation inst) throws Exception {
		final IRuntime runtime = createRuntime(inst);
		final RuntimeData data = new RuntimeData();
		String sessionId = options.getSessionId();
		if (sessionId == null) {
			sessionId = createSessionId();
		}
		data.setSessionId(sessionId);
		runtime.startup(data);
		inst.addTransformer(new CoverageTransformer(runtime, options, logger));
		controller = createAgentController();
		controller.startup(options, data);
	}

	/**
	 * Create controller implementation as given by the agent options.
	 * 
	 * @return configured controller implementation
	 */
	protected IAgentController createAgentController() {
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

	/**
	 * Creates the specific coverage runtime implementation.
	 * 
	 * @param inst
	 *            instrumentation services
	 * @return coverage runtime instance
	 * @throws Exception
	 *             creation problem
	 */
	protected IRuntime createRuntime(final Instrumentation inst)
			throws Exception {
		return ModifiedSystemClassRuntime.createFor(inst, "java/util/UUID");
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
	 * This method is called by the JVM to initialize Java agents.
	 * 
	 * @param options
	 *            agent options
	 * @param inst
	 *            instrumentation callback provided by the JVM
	 * @throws Exception
	 *             in case initialization fails
	 */
	public static void premain(final String options, final Instrumentation inst)
			throws Exception {

		final JacocoAgent agent = new JacocoAgent(options,
				new IExceptionLogger() {
					public void logExeption(final Exception ex) {
						ex.printStackTrace();
					}
				});

		agent.init(inst);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				agent.shutdown();
			}
		});
	}

}
