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
package org.jacoco.agent.rt;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.jacoco.agent.rt.controller.IAgentController;
import org.jacoco.agent.rt.controller.LocalAgentController;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.ModifiedSystemClassRuntime;

/**
 * The agent which is referred as the <code>Premain-Class</code>.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class JacocoAgent {

	private final AgentOptions options;

	private IRuntime runtime;
	private IAgentController output;

	/**
	 * Creates a new agent with the given agent options.
	 * 
	 * @param options
	 *            agent options
	 */
	public JacocoAgent(AgentOptions options) {
		this.options = options;
	}

	/**
	 * Creates a new agent with the given agent options string.
	 * 
	 * @param options
	 *            agent options as text string
	 */
	public JacocoAgent(String options) {
		this(new AgentOptions(options));
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
		inst.addTransformer(new CoverageTransformer(runtime, options));
		output = createAgentController();
		output.startup(options, runtime);
	}

	private IAgentController createAgentController() {
		OutputMode controllerType = options.getOutput();
		switch (controllerType) {
		case file:
			return new LocalAgentController();
		default:
			throw new IllegalArgumentException(String.format(
					"Unsupported agent controller type: %s", controllerType));
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
		return ModifiedSystemClassRuntime.createFor(inst, "java/sql/Types");
	}

	/**
	 * Shutdown the agent again.
	 */
	public void shutdown() {
		if (options.getDumpOnExit()) {
			try {
				output.writeExecutionData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		output.shutdown();
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
		final JacocoAgent agent = new JacocoAgent(options);
		agent.init(inst);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				agent.shutdown();
			}
		});
	}

}
