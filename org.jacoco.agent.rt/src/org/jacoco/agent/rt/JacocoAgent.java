/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.Random;

import org.jacoco.agent.rt.controller.IAgentController;
import org.jacoco.agent.rt.controller.LocalController;
import org.jacoco.agent.rt.controller.TcpClientController;
import org.jacoco.agent.rt.controller.TcpServerController;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.ModifiedSystemClassRuntime;

/**
 * The agent which is referred as the <code>Premain-Class</code>.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class JacocoAgent {

	private final AgentOptions options;

	private final IExceptionLogger logger;

	private IRuntime runtime;

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
	 */
	public JacocoAgent(String options, IExceptionLogger logger) {
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
		runtime = createRuntime(inst);
		String sessionId = options.getSessionId();
		if (sessionId == null) {
			sessionId = createSessionId();
		}
		runtime.setSessionId(sessionId);
		runtime.startup();
		inst.addTransformer(new CoverageTransformer(runtime, options, logger));
		controller = createAgentController();
		controller.startup(options, runtime);
	}

	private IAgentController createAgentController() {
		OutputMode controllerType = options.getOutput();
		switch (controllerType) {
		case file:
			return new LocalController();
		case tcpserver:
			return new TcpServerController(logger);
		case tcpclient:
			return new TcpClientController(logger);
		default:
			throw new AssertionError(controllerType);
		}
	}

	private String createSessionId() {
		String host;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			host = "unknownhost";
		}
		return host + "-" + Integer.toHexString(new Random().nextInt());
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
		} catch (Exception e) {
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
	 */
	public static void premain(final String options, final Instrumentation inst)
			throws Exception {

		final JacocoAgent agent = new JacocoAgent(options,
				new IExceptionLogger() {
					public void logExeption(Exception ex) {
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
