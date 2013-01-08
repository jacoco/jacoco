/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jacoco.agent.AgentJar;
import org.jacoco.core.runtime.AgentOptions;

/**
 * Base class for all coverage tasks that require agent options
 */
public class AbstractCoverageTask extends Task {

	private final AgentOptions agentOptions;

	private boolean enabled;

	/**
	 * Create default agent options
	 */
	protected AbstractCoverageTask() {
		super();
		agentOptions = new AgentOptions();
		enabled = true;
	}

	/**
	 * @return Whether or not the current task is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether or not the current task is enabled
	 * 
	 * @param enabled
	 *            Enablement state of the task
	 */
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
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
	 * Sets the location to write coverage execution data. Default is current
	 * working directory
	 * 
	 * @param file
	 *            Location to write coverage execution data
	 */
	public void setDestfile(final File file) {
		agentOptions.setDestfile(file.getAbsolutePath());
	}

	/**
	 * Append execution coverage data if a coverage file is already present.
	 * Default is <code>true</code>
	 * 
	 * @param append
	 *            <code>true</code> to append execution data to an existing file
	 */
	public void setAppend(final boolean append) {
		agentOptions.setAppend(append);
	}

	/**
	 * List of wildcard patterns classes to include for instrumentation. Default
	 * is <code>*</code>
	 * 
	 * @param includes
	 *            Wildcard pattern of included classes
	 */
	public void setIncludes(final String includes) {
		agentOptions.setIncludes(includes);
	}

	/**
	 * List of wildcard patterns classes to exclude from instrumentation.
	 * Default is the empty string, no classes excluded
	 * 
	 * @param excludes
	 *            Wildcard pattern of excluded classes
	 */
	public void setExcludes(final String excludes) {
		agentOptions.setExcludes(excludes);
	}

	/**
	 * List of wildcard patterns for classloaders that JaCoCo will not
	 * instrument classes from. Default is
	 * <code>sun.reflect.DelegatingClassLoader</code>
	 * 
	 * @param exclClassLoader
	 *            Wildcard pattern of class loaders to exclude
	 */
	public void setExclClassLoader(final String exclClassLoader) {
		agentOptions.setExclClassloader(exclClassLoader);
	}

	/**
	 * Sets the session identifier. Default is a auto-generated id
	 * 
	 * @param id
	 *            session identifier
	 */
	public void setSessionId(final String id) {
		agentOptions.setSessionId(id);
	}

	/**
	 * Dump coverage data on VM termination. Default is <code>true</code>
	 * 
	 * @param dumpOnExit
	 *            <code>true</code> to write coverage data on VM termination
	 */
	public void setDumpOnExit(final boolean dumpOnExit) {
		agentOptions.setDumpOnExit(dumpOnExit);
	}

	/**
	 * Sets the output method. Default is <code>file</code>
	 * 
	 * @param output
	 *            Output method
	 */
	public void setOutput(final String output) {
		agentOptions.setOutput(output);
	}

	/**
	 * Sets the IP address or hostname to bind to when output method is tcp
	 * server or connect to when the output method is tcp client. Default is
	 * <code>localhost</code>
	 * 
	 * @param address
	 *            Address to bind or connect to
	 */
	public void setAddress(final String address) {
		agentOptions.setAddress(address);
	}

	/**
	 * Sets the Port to bind to when the output method is tcp server or connect
	 * to when the output method is tcp client. Default is <code>6300</code>
	 * 
	 * @param port
	 */
	public void setPort(final int port) {
		agentOptions.setPort(port);
	}

	/**
	 * Sets the directory where all class files seen by the agent should be
	 * dumped to.
	 * 
	 * @param dir
	 *            dump output location
	 */
	public void setClassdumpdir(final File dir) {
		agentOptions.setClassDumpDir(dir.getAbsolutePath());
	}

	/**
	 * Sets whether the agent should expose functionality via JMX.
	 * 
	 * @param jmx
	 *            <code>true</code> if JMX should be enabled
	 */
	public void setJmx(final boolean jmx) {
		agentOptions.setJmx(jmx);
	}

	/**
	 * Creates JVM argument to launch with the specified JaCoCo agent jar and
	 * the current options
	 * 
	 * @return JVM Argument to pass to new VM
	 */
	protected String getLaunchingArgument() {
		return getAgentOptions().getVMArgument(getAgentFile());
	}

	private File getAgentFile() {
		try {
			File agentFile = null;
			final String agentFileLocation = getProject().getProperty(
					"_jacoco.agentFile");
			if (agentFileLocation != null) {
				agentFile = new File(agentFileLocation);
			} else {
				agentFile = AgentJar.extractToTempLocation();
				getProject().setProperty("_jacoco.agentFile",
						agentFile.toString());
			}

			return agentFile;
		} catch (final IOException e) {
			throw new BuildException("Unable to extract agent jar", e,
					getLocation());
		}
	}

}
