/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Chas Honton - initial implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;

/**
 * Start a jacoco tcp server collector
 * 
 * @goal start-tcpserver-collector
 * 
 * @phase process-test-classes
 */
public class StartServerCollectorMojo extends AbstractJacocoMojo {

	/**
	 * Allows to specify property which will contains settings for JaCoCo Agent.
	 * If not specified, then "argLine" would be used for "jar" packaging and
	 * "tycho.testArgLine" for "eclipse-test-plugin".
	 * 
	 * @parameter expression="${jacoco.propertyName}" default-value="argLine"
	 */
	private String propertyName;

	/**
	 * Project properties
	 * 
	 * @parameter expression="${project.properties}"
	 * @required
	 * @readonly
	 */
	private Properties projectProperties;

	/**
	 * Execution Id
	 * 
	 * @parameter default-value="${mojoExecution.executionId}"
	 * @required
	 * @readonly
	 */
	private String executionId;

	@Override
	public void executeMojo() throws MojoExecutionException {
		try {
			final CollectorServer server = new CollectorServer(
					getOutputStream(), getServerSocket(), getDumpOnExit(),
					getLog());
			saveInstance(server);
		} catch (final IOException e) {
			throw new MojoExecutionException("Error starting tcp-server", e);
		}
	}

	private void saveInstance(final Object instance) {
		final String instanceName = instance.getClass().getCanonicalName()
				+ '#' + executionId;
		projectProperties.put(instanceName, instance);
	}

	private ServerSocket getServerSocket() throws MojoExecutionException {
		final AgentOptions agentOptions = getAgentOptions();
		InetAddress inetAddress = null;
		final String address = agentOptions.getAddress();
		if (address != null) {
			try {
				inetAddress = InetAddress.getByName(address);
			} catch (final UnknownHostException e) {
				throw new MojoExecutionException("Can not resolve " + address,
						e);
			}
		}
		final InetSocketAddress endpoint = new InetSocketAddress(inetAddress,
				agentOptions.getPort());

		try {
			final ServerSocket server = new ServerSocket();
			server.bind(endpoint);
			return server;
		} catch (final IOException e) {
			throw new MojoExecutionException("Can not open server socket "
					+ endpoint, e);
		}
	}

	private OutputStream getOutputStream() throws MojoExecutionException {
		final AgentOptions agentOptions = getAgentOptions();
		final String destFile = agentOptions.getDestfile();
		final boolean append = agentOptions.getAppend();
		try {
			return new FileOutputStream(destFile, append);
		} catch (final FileNotFoundException e) {
			throw new MojoExecutionException("Can not find " + destFile, e);
		}
	}

	private boolean getDumpOnExit() throws MojoExecutionException {
		return getAgentOptions().getDumpOnExit();
	}

	private AgentOptions agentOptions;

	private AgentOptions getAgentOptions() throws MojoExecutionException {
		if (agentOptions == null) {
			agentOptions = new AgentOptions(getAgentProperties());
			if (OutputMode.tcpclient != agentOptions.getOutput()) {
				throw new MojoExecutionException(
						"Expecting 'tcpclient' output mode in property '"
								+ propertyName + "'");
			}
		}
		return agentOptions;
	}

	static final Pattern AGENT_PARSER = Pattern
			.compile("(\"?)-javaagent:[^=]+=(.*) ?");

	private String getAgentProperties() throws MojoExecutionException {
		final String argLine = getArgLineProperty();
		getLog().debug("argLine='" + argLine + "'");
		final Matcher javaAgentMatch = AGENT_PARSER.matcher(argLine);
		if (!javaAgentMatch.find()) {
			throw new MojoExecutionException("Property '" + propertyName
					+ "' (" + argLine + ") does not have '-javaagent:'");
		}

		final boolean notQuoted = javaAgentMatch.group(1).isEmpty();
		if (notQuoted) {
			// use up to space (or end of argument)
			final String upToSpace = javaAgentMatch.group(2);
			getLog().debug("after javaAgent='" + upToSpace + "'");
			return upToSpace;
		}
		return unescape(argLine, javaAgentMatch.start(1),
				javaAgentMatch.start(2));
	}

	private String unescape(final String arg, final int quote, final int equal) {
		final StringBuilder sb = new StringBuilder(arg.length() - equal);
		for (int i = equal; i < arg.length(); ++i) {
			char c = arg.charAt(i);
			switch (c) {
			case '"':
				return sb.toString();
			case '\\':
				if (++i < arg.length()) {
					c = arg.charAt(i);
				}
				// fall into default
			default:
				sb.append(c);
			}
		}
		getLog().debug(
				arg.substring(quote) + " not properly quoted and escaped");
		return sb.toString();
	}

	private String getArgLineProperty() throws MojoExecutionException {
		final Object optValue = projectProperties.getProperty(propertyName);
		if (!(optValue instanceof String)) {
			throw new MojoExecutionException("Property '" + propertyName
					+ "' (" + optValue + ") does not specify AgentOptions");
		}
		return (String) optValue;
	}
}
