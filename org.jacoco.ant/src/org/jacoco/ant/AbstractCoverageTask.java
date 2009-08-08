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

import java.io.File;

import org.apache.tools.ant.Task;
import org.jacoco.core.runtime.AgentOptions;

/**
 * Base class for all coverage tasks that require agent options
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class AbstractCoverageTask extends Task {

	private final AgentOptions agentOptions;

	/**
	 * Create default agent options
	 */
	protected AbstractCoverageTask() {
		agentOptions = new AgentOptions();
	}

	/**
	 * Gets the currently configured agent options for this task
	 * 
	 * @return Configured agent options
	 */
	public AgentOptions getAgentOptions() {
		return agentOptions;
	}

	/**
	 * Sets the location to write coverage execution data
	 * 
	 * @ant.not-required Default is current working directory
	 * @param file
	 *            Location to write coverage execution data
	 */
	public void setFile(final File file) {
		agentOptions.setFile(file.getAbsolutePath());
	}

	/**
	 * Merge execution coverage data if a coverage file is already present
	 * 
	 * @ant.not-required Default is true
	 * @param merge
	 *            <code>true</code> to merge execution data
	 */
	public void setMerge(final boolean merge) {
		agentOptions.setMerge(merge);
	}

	/**
	 * Space separated list of wildcard patterns for classloaders that JaCoCo
	 * will not instrument classes from
	 * 
	 * @ant.not-required Default is sun.reflect.DelegatingClassLoader
	 * @param exclClassLoader
	 *            Wildcard pattern of class loaders to exclude
	 */
	public void setExclClassLoader(final String exclClassLoader) {
		agentOptions.setExclClassloader(exclClassLoader);
	}

}
