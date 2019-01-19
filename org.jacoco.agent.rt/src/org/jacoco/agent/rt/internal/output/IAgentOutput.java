/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import java.io.IOException;

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Common interface for different implementations that outputs execution data
 * dumps.
 */
public interface IAgentOutput {

	/**
	 * Configure the agent controller with the supplied options and connect it
	 * to the coverage runtime
	 * 
	 * @param options
	 *            Options used to configure the agent controller
	 * @param data
	 *            Execution data for this agent
	 * @throws Exception
	 *             in case startup fails
	 */
	void startup(AgentOptions options, RuntimeData data) throws Exception;

	/**
	 * Shutdown the agent controller and clean up any resources it has created.
	 * 
	 * @throws Exception
	 *             in case shutdown fails
	 */
	void shutdown() throws Exception;

	/**
	 * Write all execution data in the runtime to a location determined by the
	 * agent controller. This method should only be called by the Agent
	 * 
	 * @param reset
	 *            if <code>true</code> execution data is cleared afterwards
	 * @throws IOException
	 *             in case writing fails
	 */
	void writeExecutionData(boolean reset) throws IOException;

}
