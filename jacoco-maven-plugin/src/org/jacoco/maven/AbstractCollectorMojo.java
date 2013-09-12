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

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;

/**
 * Base class for collector mojos ({@link DumpMojo},
 * {@link StartServerCollectorMojo}
 */
public abstract class AbstractCollectorMojo extends AbstractJacocoMojo {

	/**
	 * Specify the property which contains settings for JaCoCo Agent. If not
	 * specified, then "argLine" will be used.
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
	protected Properties projectProperties;

	/**
	 * The cached instance of the parsed argLine
	 */
	private AgentOptions agentOptions;

	static final Pattern AGENT_PARSER = Pattern
			.compile("(\"?)-javaagent:[^=]+=(.*) ?");

	/**
	 * Get the configuration from the property set by an {@link AgentMojo}, and
	 * make sure the output mode is correct
	 * 
	 * @param expectedMode
	 *            The expected output mode
	 * 
	 * @return The configured options
	 * 
	 * @throws MojoExecutionException
	 */
	protected AgentOptions getConfiguration(final OutputMode expectedMode)
			throws MojoExecutionException {
		final AgentOptions options = getConfiguration();
		if (expectedMode != options.getOutput()) {
			throw new MojoExecutionException("Expecting '" + expectedMode
					+ "' output mode in property '" + propertyName + "'");
		}
		return options;
	}

	/**
	 * Get the configuration from the property set by an {@link AgentMojo}
	 * 
	 * @return The configured options
	 * 
	 * @throws MojoExecutionException
	 */
	private AgentOptions getConfiguration() throws MojoExecutionException {
		if (agentOptions == null) {
			agentOptions = new AgentOptions(getAgentProperties());
		}
		return agentOptions;
	}

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