/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import org.jacoco.core.runtime.AgentOptions;

/**
 * Base class for preparing a property pointing to the JaCoCo runtime agent that
 * can be passed as a VM argument to the application under test.
 */
public abstract class AbstractAgentMojo extends AbstractJacocoMojo {

	/**
	 * Name of the JaCoCo Agent artifact.
	 */
	static final String AGENT_ARTIFACT_NAME = "org.jacoco:org.jacoco.agent";
	/**
	 * Name of the property used in maven-osgi-test-plugin.
	 */
	static final String TYCHO_ARG_LINE = "tycho.testArgLine";
	/**
	 * Name of the property used in maven-surefire-plugin.
	 */
	static final String SUREFIRE_ARG_LINE = "argLine";
	/**
	 * Map of plugin artifacts.
	 */
	@Parameter(property = "plugin.artifactMap", required = true, readonly = true)
	Map<String, Artifact> pluginArtifactMap;
	/**
	 * Allows to specify property which will contains settings for JaCoCo Agent.
	 * If not specified, then "argLine" would be used for "jar" packaging and
	 * "tycho.testArgLine" for "eclipse-test-plugin".
	 */
	@Parameter(property = "jacoco.propertyName")
	String propertyName;
	/**
	 * If set to true and the execution data file already exists, coverage data
	 * is appended to the existing file. If set to false, an existing execution
	 * data file will be replaced.
	 */
	@Parameter(property = "jacoco.append")
	Boolean append;

	/**
	 * A list of class names to include in instrumentation. May use wildcard
	 * characters (* and ?). When not specified everything will be included.
	 */
	@Parameter
	private List<String> includes;

	/**
	 * A list of class names to exclude from instrumentation. May use wildcard
	 * characters (* and ?). When not specified nothing will be excluded. Except
	 * for performance optimization or technical corner cases this option is
	 * normally not required. If you want to exclude classes from the report
	 * please configure the <code>report</code> goal accordingly.
	 */
	@Parameter
	private List<String> excludes;

	/**
	 * A list of class loader names, that should be excluded from execution
	 * analysis. The list entries are separated by a colon (:) and may use
	 * wildcard characters (* and ?). This option might be required in case of
	 * special frameworks that conflict with JaCoCo code instrumentation, in
	 * particular class loaders that do not have access to the Java runtime
	 * classes.
	 */
	@Parameter(property = "jacoco.exclClassLoaders")
	String exclClassLoaders;
	/**
	 * Specifies whether also classes from the bootstrap classloader should be
	 * instrumented. Use this feature with caution, it needs heavy
	 * includes/excludes tuning.
	 */
	@Parameter(property = "jacoco.inclBootstrapClasses")
	Boolean inclBootstrapClasses;
	/**
	 * Specifies whether classes without source location should be instrumented.
	 */
	@Parameter(property = "jacoco.inclNoLocationClasses")
	Boolean inclNoLocationClasses;
	/**
	 * A session identifier that is written with the execution data. Without
	 * this parameter a random identifier is created by the agent.
	 */
	@Parameter(property = "jacoco.sessionId")
	String sessionId;
	/**
	 * If set to true coverage data will be written on VM shutdown.
	 */
	@Parameter(property = "jacoco.dumpOnExit")
	Boolean dumpOnExit;
	/**
	 * Output method to use for writing coverage data. Valid options are:
	 * <ul>
	 * <li>file: At VM termination execution data is written to a file.</li>
	 * <li>tcpserver: The agent listens for incoming connections on the TCP port
	 * specified by the {@link #address} and {@link #port}. Execution data is
	 * written to this TCP connection.</li>
	 * <li>tcpclient: At startup the agent connects to the TCP port specified by
	 * the {@link #address} and {@link #port}. Execution data is written to this
	 * TCP connection.</li>
	 * <li>none: Do not produce any output.</li>
	 * </ul>
	 */
	@Parameter(property = "jacoco.output")
	String output;
	/**
	 * IP address or hostname to bind to when the output method is tcpserver or
	 * connect to when the output method is tcpclient. In tcpserver mode the
	 * value "*" causes the agent to accept connections on any local address.
	 */
	@Parameter(property = "jacoco.address")
	String address;
	/**
	 * Port to bind to when the output method is tcpserver or connect to when
	 * the output method is tcpclient. In tcpserver mode the port must be
	 * available, which means that if multiple JaCoCo agents should run on the
	 * same machine, different ports have to be specified.
	 */
	@Parameter(property = "jacoco.port")
	Integer port;
	/**
	 * If a directory is specified for this parameter the JaCoCo agent dumps all
	 * class files it processes to the given location. This can be useful for
	 * debugging purposes or in case of dynamically created classes for example
	 * when scripting engines are used.
	 */
	@Parameter(property = "jacoco.classDumpDir")
	File classDumpDir;
	/**
	 * If set to true the agent exposes functionality via JMX.
	 */
	@Parameter(property = "jacoco.jmx")
	Boolean jmx;

	@Override
	public void executeMojo() {
		final String name = getEffectivePropertyName();
		final Properties projectProperties = getProject().getProperties();
		final String oldValue = projectProperties.getProperty(name);
		final String newValue = createAgentOptions()
				.prependVMArguments(oldValue, getAgentJarFile());
		getLog().info(name + " set to " + newValue);
		projectProperties.setProperty(name, newValue);
	}

	@Override
	protected void skipMojo() {
		final String name = getEffectivePropertyName();
		final Properties projectProperties = getProject().getProperties();
		final String oldValue = projectProperties.getProperty(name);
		if (oldValue == null) {
			getLog().info(name + " set to empty");
			projectProperties.setProperty(name, "");
		}
	}

	File getAgentJarFile() {
		final Artifact jacocoAgentArtifact = pluginArtifactMap
				.get(AGENT_ARTIFACT_NAME);
		return jacocoAgentArtifact.getFile();
	}

	AgentOptions createAgentOptions() {
		final AgentOptions agentOptions = new AgentOptions();
		agentOptions.setDestfile(getDestFile().getAbsolutePath());
		if (append != null) {
			agentOptions.setAppend(append.booleanValue());
		}
		if (includes != null && !includes.isEmpty()) {
			agentOptions
					.setIncludes(StringUtils.join(includes.iterator(), ":"));
		}
		if (excludes != null && !excludes.isEmpty()) {
			agentOptions
					.setExcludes(StringUtils.join(excludes.iterator(), ":"));
		}
		if (exclClassLoaders != null) {
			agentOptions.setExclClassloader(exclClassLoaders);
		}
		if (inclBootstrapClasses != null) {
			agentOptions.setInclBootstrapClasses(
					inclBootstrapClasses.booleanValue());
		}
		if (inclNoLocationClasses != null) {
			agentOptions.setInclNoLocationClasses(
					inclNoLocationClasses.booleanValue());
		}
		if (sessionId != null) {
			agentOptions.setSessionId(sessionId);
		}
		if (dumpOnExit != null) {
			agentOptions.setDumpOnExit(dumpOnExit.booleanValue());
		}
		if (output != null) {
			agentOptions.setOutput(output);
		}
		if (address != null) {
			agentOptions.setAddress(address);
		}
		if (port != null) {
			agentOptions.setPort(port.intValue());
		}
		if (classDumpDir != null) {
			agentOptions.setClassDumpDir(classDumpDir.getAbsolutePath());
		}
		if (jmx != null) {
			agentOptions.setJmx(jmx.booleanValue());
		}
		return agentOptions;
	}

	String getEffectivePropertyName() {
		if (isPropertyNameSpecified()) {
			return propertyName;
		}
		if (isEclipseTestPluginPackaging()) {
			return TYCHO_ARG_LINE;
		}
		return SUREFIRE_ARG_LINE;
	}

	boolean isPropertyNameSpecified() {
		return propertyName != null && !"".equals(propertyName);
	}

	boolean isEclipseTestPluginPackaging() {
		return "eclipse-test-plugin".equals(getProject().getPackaging());
	}

	/**
	 * @return the destFile
	 */
	abstract File getDestFile();

}
