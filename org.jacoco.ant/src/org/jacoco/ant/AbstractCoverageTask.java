/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.ant;

import org.apache.tools.ant.Task;

/**
 * Base class for all coverage tasks that require agent options
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class AbstractCoverageTask extends Task {

	private AgentOptions agentOptions;

	/**
	 * Create default agent options
	 */
	protected AbstractCoverageTask() {
		agentOptions = new AgentOptions();
	}

	/**
	 * Gets the currently configured agent options
	 * 
	 * @return Current agent options
	 */
	public AgentOptions getAgentOptions() {
		return agentOptions;
	}

	/**
	 * Sets the agent options to use
	 * 
	 * @param agentOptions
	 *            Agent Options
	 */
	public void setAgentOptions(final AgentOptions agentOptions) {
		this.agentOptions = agentOptions;
	}

	/**
	 * Creates a new default agent options for this task
	 * 
	 * @return New agent options with default values
	 */
	public AgentOptions createAgentOptions() {
		return agentOptions = new AgentOptions();
	}

}
