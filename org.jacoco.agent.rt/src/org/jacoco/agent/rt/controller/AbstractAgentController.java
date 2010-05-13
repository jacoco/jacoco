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

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;

/**
 * Basic implementation of an Agent Controller
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public abstract class AbstractAgentController implements IAgentController {

	private AgentOptions options;
	private IRuntime runtime;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jacoco.core.output.IAgentController#startup(org.jacoco.core.runtime
	 * .AgentOptions, org.jacoco.core.runtime.IRuntime)
	 */
	public final void startup(final AgentOptions options, final IRuntime runtime) {
		this.options = options;
		this.runtime = runtime;
		startup();
	}

	/**
	 * Perform tasks required to initialize the agent controller.
	 */
	public void startup() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jacoco.core.output.IAgentController#shutdown()
	 */
	public void shutdown() {
	}

	/**
	 * Retrieves the agent options this controller is configured with
	 * 
	 * @return Configuration options
	 */
	protected AgentOptions getOptions() {
		return options;
	}

	/**
	 * Retrieves the coverage runtime being controlled
	 * 
	 * @return Runtime being controlled
	 */
	protected IRuntime getRuntime() {
		return runtime;
	}
}
