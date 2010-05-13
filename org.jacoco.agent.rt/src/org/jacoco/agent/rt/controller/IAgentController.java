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
import org.jacoco.core.runtime.IRuntime;

/**
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public interface IAgentController {

	/**
	 * Configure the agent controller with the supplied options and connect it
	 * to the coverage runtime
	 * 
	 * @param options
	 *            Options used to configure the agent controller
	 * @param runtime
	 *            Coverage runtime this agent controller will be connected to
	 */
	public void startup(final AgentOptions options, final IRuntime runtime);

	/**
	 * Shutdown the agent controller and clean up any resources it has created.
	 */
	public void shutdown();

	/**
	 * Write all execution data in the runtime to a location determined by the
	 * agent controller. This method should only be called by the Agent
	 * 
	 * @throws IOException
	 *             Error writing execution data
	 */
	public void writeExecutionData() throws IOException;

}
