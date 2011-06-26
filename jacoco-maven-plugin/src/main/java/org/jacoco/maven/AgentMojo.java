/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.StringUtils;
import org.jacoco.core.runtime.AgentOptions;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * Prepares a property pointing to the JaCoCo runtime agent that can be passed
 * as a VM argument to the application under test. Depending on the project
 * packaging type by default a property with the following name is set:
 * <ul>
 * <li>tycho.testArgLine for packaging type eclipse-test-plugin and</li>
 * <li>argLine otherwise.</li>
 * </ul>
 * Resulting coverage information is collected during execution and by default
 * written to a file when the process terminates.
 * 
 * @phase initialize
 * @goal prepare-agent
 * @requiresProject true
 * @requiresDependencyResolution runtime
 */
public class AgentMojo extends AbstractJacocoMojo {

	/**
	 * Name of the JaCoCo Agent artifact.
	 */
	private static final String AGENT_ARTIFACT_NAME = "org.jacoco:org.jacoco.agent.rt";

	/**
	 * Name of the property used in maven-osgi-test-plugin.
	 */
	private static final String TYCHO_ARG_LINE = "tycho.testArgLine";

	/**
	 * Name of the property used in maven-surefire-plugin.
	 */
	private static final String SUREFIRE_ARG_LINE = "argLine";

	/**
	 * Map of plugin artifacts.
	 * 
	 * @parameter expression="${plugin.artifactMap}"
	 * @required
	 * @readonly
	 */
	private Map<String, Artifact> pluginArtifactMap;

	/**
	 * Allows to specify property which will contains settings for JaCoCo Agent.
	 * If not specified, then "argLine" would be used for "jar" packaging and
	 * "tycho.testArgLine" for "eclipse-test-plugin".
	 * 
	 * @parameter expression="${jacoco.propertyName}"
	 */
	private String propertyName;

	/**
	 * Path to the output file for execution data.
	 * 
	 * @parameter expression="${jacoco.destfile}"
	 *            default-value="${project.build.directory}/jacoco.exec"
	 */
	private File destfile;

	/**
	 * If set to true and the execution data file already exists, coverage data
	 * is appended to the existing file. If set to false, an existing execution
	 * data file will be replaced.
	 * 
	 * @parameter expression="${jacoco.append}"
	 */
	private Boolean append;

	/**
	 * A list of class names that should be included in execution analysis. The
	 * list entries are separated by a colon (:) and may use wildcard characters
	 * (* and ?). Except for performance optimization or technical corner cases
	 * this option is normally not required.
	 * 
	 * @parameter expression="${jacoco.includes}"
	 */
	private String includes;

	/**
	 * A list of class names that should be excluded from execution analysis.
	 * 
	 * The list entries are separated by a colon (:) and may use wildcard
	 * characters (* and ?). Except for performance optimization or technical
	 * corner cases this option is normally not required.
	 * 
	 * @parameter expression="${jacoco.excludes}"
	 */
	private String excludes;

	/**
	 * A list of class loader names, that should be excluded from execution
	 * analysis. The list entries are separated by a colon (:) and may use
	 * wildcard characters (* and ?). This option might be required in case of
	 * special frameworks that conflict with JaCoCo code instrumentation, in
	 * particular class loaders that do not have access to the Java runtime
	 * classes.
	 * 
	 * @parameter expression="${jacoco.exclclassloaders}"
	 */
	private String exclclassloaders;

	/**
	 * A session identifier that is written with the execution data. Without
	 * this parameter a random identifier is created by the agent.
	 * 
	 * @parameter expression="${jacoco.sessionid}"
	 */
	private String sessionid;

	/**
	 * If set to true coverage data will be written on VM shutdown.
	 * 
	 * @parameter expression="${jacoco.dumponexit}"
	 */
	private Boolean dumpOnExit;

	/**
	 * Output method to use for writing coverage data. Valid options are:
	 * <ul>
	 * <li>file: At VM termination execution data is written to the file
	 * specified in the {@link #destfile}.</li>
	 * <li>tcpserver: The agent listens for incoming connections on the TCP port
	 * specified by the {@link #address} and {@link #port}. Execution data is
	 * written to this TCP connection.</li>
	 * <li>tcpclient: At startup the agent connects to the TCP port specified by
	 * the {@link #address} and {@link #port}. Execution data is written to this
	 * TCP connection.</li>
	 * <li>mbean: The agent registers an JMX MBean under the name
	 * <code>org.jacoco:type=Runtime</code>.</li>
	 * </ul>
	 * 
	 * @parameter expression="${jacoco.output}"
	 */
	private String output;

	/**
	 * IP address or hostname to bind to when the output method is tcpserver or
	 * connect to when the output method is tcpclient. In tcpserver mode the
	 * value "*" causes the agent to accept connections on any local address.
	 * 
	 * @parameter expression="${jacoco.address}"
	 */
	private String address;

	/**
	 * Port to bind to when the output method is tcpserver or connect to when
	 * the output method is tcpclient. In tcpserver mode the port must be
	 * available, which means that if multiple JaCoCo agents should run on the
	 * same machine, different ports have to be specified.
	 * 
	 * @parameter expression="${jacoco.port}"
	 */
	private Integer port;

	public void executeMojo() {
		String vmArgument = StringUtils.quoteAndEscape(createAgentOptions()
				.getVMArgument(getAgentJarFile()), '"');
		if (isPropertyNameSpecified()) {
			prependProperty(propertyName, vmArgument);
		} else if (isEclipseTestPluginPackaging()) {
			prependProperty(TYCHO_ARG_LINE, vmArgument);
		} else {
			prependProperty(SUREFIRE_ARG_LINE, vmArgument);
		}
	}

	private File getAgentJarFile() {
		Artifact jacocoAgentArtifact = pluginArtifactMap
				.get(AGENT_ARTIFACT_NAME);
		return jacocoAgentArtifact.getFile();
	}

	private AgentOptions createAgentOptions() {
		AgentOptions agentOptions = new AgentOptions();
		String destPath = destfile.getAbsolutePath();
		agentOptions.setDestfile(destPath);
		if (append != null) {
			agentOptions.setAppend(append);
		}
		if (includes != null) {
			agentOptions.setIncludes(includes);
		}
		if (excludes != null) {
			agentOptions.setExcludes(excludes);
		}
		if (exclclassloaders != null) {
			agentOptions.setExclClassloader(exclclassloaders);
		}
		if (sessionid != null) {
			agentOptions.setSessionId(sessionid);
		}
		if (dumpOnExit != null) {
			agentOptions.setDumpOnExit(dumpOnExit);
		}
		if (output != null) {
			agentOptions.setOutput(output);
		}
		if (address != null) {
			agentOptions.setAddress(address);
		}
		if (port != null) {
			agentOptions.setPort(port);
		}
		return agentOptions;
	}

	private boolean isPropertyNameSpecified() {
		return propertyName != null && !"".equals(propertyName);
	}

	private boolean isEclipseTestPluginPackaging() {
		return "eclipse-test-plugin".equals(getProject().getPackaging());
	}

	private void prependProperty(String name, String value) {
		Properties projectProperties = getProject().getProperties();
		String oldValue = projectProperties.getProperty(name);
		String newValue = oldValue == null ? value : value + ' ' + oldValue;
		getLog().info(name + " set to " + newValue);
		projectProperties.put(name, newValue);
	}

}
