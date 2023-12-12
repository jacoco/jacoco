/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.runtime;

import static java.lang.String.format;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Utility to create and parse options for the runtime agent. Options are
 * represented as a string in the following format:
 *
 * <pre>
 *   key1=value1,key2=value2,key3=value3
 * </pre>
 */
public final class AgentOptions {

	/**
	 * Specifies the output file for execution data. Default is
	 * <code>jacoco.exec</code> in the working directory.
	 */
	public static final String DESTFILE = "destfile";

	/**
	 * Default value for the "destfile" agent option.
	 */
	public static final String DEFAULT_DESTFILE = "jacoco.exec";

	/**
	 * Specifies whether execution data should be appended to the output file.
	 * Default is <code>true</code>.
	 */
	public static final String APPEND = "append";

	/**
	 * Wildcard expression for class names that should be included for code
	 * coverage. Default is <code>*</code> (all classes included).
	 *
	 * @see WildcardMatcher
	 */
	public static final String INCLUDES = "includes";

	/**
	 * Wildcard expression for class names that should be excluded from code
	 * coverage. Default is the empty string (no exclusions).
	 *
	 * @see WildcardMatcher
	 */
	public static final String EXCLUDES = "excludes";

	/**
	 * Wildcard expression for class loaders names for classes that should be
	 * excluded from code coverage. This means all classes loaded by a class
	 * loader which full qualified name matches this expression will be ignored
	 * for code coverage regardless of all other filtering settings. Default is
	 * <code>sun.reflect.DelegatingClassLoader</code>.
	 *
	 * @see WildcardMatcher
	 */
	public static final String EXCLCLASSLOADER = "exclclassloader";

	/**
	 * Specifies whether also classes from the bootstrap classloader should be
	 * instrumented. Use this feature with caution, it needs heavy
	 * includes/excludes tuning. Default is <code>false</code>.
	 */
	public static final String INCLBOOTSTRAPCLASSES = "inclbootstrapclasses";

	/**
	 * Specifies whether also classes without a source location should be
	 * instrumented. Normally such classes are generated at runtime e.g. by
	 * mocking frameworks and are therefore excluded by default. Default is
	 * <code>false</code>.
	 */
	public static final String INCLNOLOCATIONCLASSES = "inclnolocationclasses";

	/**
	 * Specifies a session identifier that is written with the execution data.
	 * Without this parameter a random identifier is created by the agent.
	 */
	public static final String SESSIONID = "sessionid";

	/**
	 * Specifies whether the agent will automatically dump coverage data on VM
	 * exit. Default is <code>true</code>.
	 */
	public static final String DUMPONEXIT = "dumponexit";

	/**
	 * Specifies the output mode. Default is {@link OutputMode#file}.
	 *
	 * @see OutputMode#file
	 * @see OutputMode#tcpserver
	 * @see OutputMode#tcpclient
	 * @see OutputMode#none
	 */
	public static final String OUTPUT = "output";

	private static final Pattern OPTION_SPLIT = Pattern
			.compile(",(?=[a-zA-Z0-9_\\-]+=)");

	/**
	 * Possible values for {@link AgentOptions#OUTPUT}.
	 */
	public static enum OutputMode {

		/**
		 * Value for the {@link AgentOptions#OUTPUT} parameter: At VM
		 * termination execution data is written to the file specified by
		 * {@link AgentOptions#DESTFILE}.
		 */
		file,

		/**
		 * Value for the {@link AgentOptions#OUTPUT} parameter: The agent
		 * listens for incoming connections on a TCP port specified by
		 * {@link AgentOptions#ADDRESS} and {@link AgentOptions#PORT}.
		 */
		tcpserver,

		/**
		 * Value for the {@link AgentOptions#OUTPUT} parameter: At startup the
		 * agent connects to a TCP port specified by the
		 * {@link AgentOptions#ADDRESS} and {@link AgentOptions#PORT} attribute.
		 */
		tcpclient,

		/**
		 * Value for the {@link AgentOptions#OUTPUT} parameter: Do not produce
		 * any output.
		 */
		none

	}

	/**
	 * The IP address or DNS name the tcpserver binds to or the tcpclient
	 * connects to. Default is defined by {@link #DEFAULT_ADDRESS}.
	 */
	public static final String ADDRESS = "address";

	/**
	 * Default value for the "address" agent option.
	 */
	public static final String DEFAULT_ADDRESS = null;

	/**
	 * The port the tcpserver binds to or the tcpclient connects to. In
	 * tcpserver mode the port must be available, which means that if multiple
	 * JaCoCo agents should run on the same machine, different ports have to be
	 * specified. Default is defined by {@link #DEFAULT_PORT}.
	 */
	public static final String PORT = "port";

	/**
	 * Default value for the "port" agent option.
	 */
	public static final int DEFAULT_PORT = 6300;

	/**
	 * Specifies where the agent dumps all class files it encounters. The
	 * location is specified as a relative path to the working directory.
	 * Default is <code>null</code> (no dumps).
	 */
	public static final String CLASSDUMPDIR = "classdumpdir";

	/**
	 * Specifies whether the agent should expose functionality via JMX under the
	 * name "org.jacoco:type=Runtime". Default is <code>false</code>.
	 */
	public static final String JMX = "jmx";

	private static final Collection<String> VALID_OPTIONS = Arrays.asList(
			DESTFILE, APPEND, INCLUDES, EXCLUDES, EXCLCLASSLOADER,
			INCLBOOTSTRAPCLASSES, INCLNOLOCATIONCLASSES, SESSIONID, DUMPONEXIT,
			OUTPUT, ADDRESS, PORT, CLASSDUMPDIR, JMX);

	private final Map<String, String> options;

	/**
	 * New instance with all values set to default.
	 */
	public AgentOptions() {
		this.options = new HashMap<String, String>();
	}

	/**
	 * New instance parsed from the given option string.
	 *
	 * @param optionstr
	 *            string to parse or <code>null</code>
	 */
	public AgentOptions(final String optionstr) {
		this();
		if (optionstr != null && optionstr.length() > 0) {
			for (final String entry : OPTION_SPLIT.split(optionstr)) {
				final int pos = entry.indexOf('=');
				if (pos == -1) {
					throw new IllegalArgumentException(format(
							"Invalid agent option syntax \"%s\".", optionstr));
				}
				final String key = entry.substring(0, pos);
				if (!VALID_OPTIONS.contains(key)) {
					throw new IllegalArgumentException(
							format("Unknown agent option \"%s\".", key));
				}

				final String value = entry.substring(pos + 1);
				setOption(key, value);
			}

			validateAll();
		}
	}

	/**
	 * New instance read from the given {@link Properties} object.
	 *
	 * @param properties
	 *            {@link Properties} object to read configuration options from
	 */
	public AgentOptions(final Properties properties) {
		this();
		for (final String key : VALID_OPTIONS) {
			final String value = properties.getProperty(key);
			if (value != null) {
				setOption(key, value);
			}
		}
	}

	private void validateAll() {
		validatePort(getPort());
		getOutput();
	}

	private void validatePort(final int port) {
		if (port < 0) {
			throw new IllegalArgumentException("port must be positive");
		}
	}

	/**
	 * Returns the output file location.
	 *
	 * @return output file location
	 */
	public String getDestfile() {
		return getOption(DESTFILE, DEFAULT_DESTFILE);
	}

	/**
	 * Sets the output file location.
	 *
	 * @param destfile
	 *            output file location
	 */
	public void setDestfile(final String destfile) {
		setOption(DESTFILE, destfile);
	}

	/**
	 * Returns whether the output should be appended to an existing file.
	 *
	 * @return <code>true</code>, when the output should be appended
	 */
	public boolean getAppend() {
		return getOption(APPEND, true);
	}

	/**
	 * Sets whether the output should be appended to an existing file.
	 *
	 * @param append
	 *            <code>true</code>, when the output should be appended
	 */
	public void setAppend(final boolean append) {
		setOption(APPEND, append);
	}

	/**
	 * Returns the wildcard expression for classes to include.
	 *
	 * @return wildcard expression for classes to include
	 * @see WildcardMatcher
	 */
	public String getIncludes() {
		return getOption(INCLUDES, "*");
	}

	/**
	 * Sets the wildcard expression for classes to include.
	 *
	 * @param includes
	 *            wildcard expression for classes to include
	 * @see WildcardMatcher
	 */
	public void setIncludes(final String includes) {
		setOption(INCLUDES, includes);
	}

	/**
	 * Returns the wildcard expression for classes to exclude.
	 *
	 * @return wildcard expression for classes to exclude
	 * @see WildcardMatcher
	 */
	public String getExcludes() {
		return getOption(EXCLUDES, "");
	}

	/**
	 * Sets the wildcard expression for classes to exclude.
	 *
	 * @param excludes
	 *            wildcard expression for classes to exclude
	 * @see WildcardMatcher
	 */
	public void setExcludes(final String excludes) {
		setOption(EXCLUDES, excludes);
	}

	/**
	 * Returns the wildcard expression for excluded class loaders.
	 *
	 * @return expression for excluded class loaders
	 * @see WildcardMatcher
	 */
	public String getExclClassloader() {
		return getOption(EXCLCLASSLOADER, "sun.reflect.DelegatingClassLoader");
	}

	/**
	 * Sets the wildcard expression for excluded class loaders.
	 *
	 * @param expression
	 *            expression for excluded class loaders
	 * @see WildcardMatcher
	 */
	public void setExclClassloader(final String expression) {
		setOption(EXCLCLASSLOADER, expression);
	}

	/**
	 * Returns whether classes from the bootstrap classloader should be
	 * instrumented.
	 *
	 * @return <code>true</code> if classes from the bootstrap classloader
	 *         should be instrumented
	 */
	public boolean getInclBootstrapClasses() {
		return getOption(INCLBOOTSTRAPCLASSES, false);
	}

	/**
	 * Sets whether classes from the bootstrap classloader should be
	 * instrumented.
	 *
	 * @param include
	 *            <code>true</code> if bootstrap classes should be instrumented
	 */
	public void setInclBootstrapClasses(final boolean include) {
		setOption(INCLBOOTSTRAPCLASSES, include);
	}

	/**
	 * Returns whether classes without source location should be instrumented.
	 *
	 * @return <code>true</code> if classes without source location should be
	 *         instrumented
	 */
	public boolean getInclNoLocationClasses() {
		return getOption(INCLNOLOCATIONCLASSES, false);
	}

	/**
	 * Sets whether classes without source location should be instrumented.
	 *
	 * @param include
	 *            <code>true</code> if classes without source location should be
	 *            instrumented
	 */
	public void setInclNoLocationClasses(final boolean include) {
		setOption(INCLNOLOCATIONCLASSES, include);
	}

	/**
	 * Returns the session identifier.
	 *
	 * @return session identifier
	 */
	public String getSessionId() {
		return getOption(SESSIONID, null);
	}

	/**
	 * Sets the session identifier.
	 *
	 * @param id
	 *            session identifier
	 */
	public void setSessionId(final String id) {
		setOption(SESSIONID, id);
	}

	/**
	 * Returns whether coverage data should be dumped on exit.
	 *
	 * @return <code>true</code> if coverage data will be written on VM exit
	 */
	public boolean getDumpOnExit() {
		return getOption(DUMPONEXIT, true);
	}

	/**
	 * Sets whether coverage data should be dumped on exit.
	 *
	 * @param dumpOnExit
	 *            <code>true</code> if coverage data should be written on VM
	 *            exit
	 */
	public void setDumpOnExit(final boolean dumpOnExit) {
		setOption(DUMPONEXIT, dumpOnExit);
	}

	/**
	 * Returns the port on which to listen to when the output is
	 * <code>tcpserver</code> or the port to connect to when output is
	 * <code>tcpclient</code>.
	 *
	 * @return port to listen on or connect to
	 */
	public int getPort() {
		return getOption(PORT, DEFAULT_PORT);
	}

	/**
	 * Sets the port on which to listen to when output is <code>tcpserver</code>
	 * or the port to connect to when output is <code>tcpclient</code>
	 *
	 * @param port
	 *            port to listen on or connect to
	 */
	public void setPort(final int port) {
		validatePort(port);
		setOption(PORT, port);
	}

	/**
	 * Gets the hostname or IP address to listen to when output is
	 * <code>tcpserver</code> or connect to when output is
	 * <code>tcpclient</code>
	 *
	 * @return Hostname or IP address
	 */
	public String getAddress() {
		return getOption(ADDRESS, DEFAULT_ADDRESS);
	}

	/**
	 * Sets the hostname or IP address to listen to when output is
	 * <code>tcpserver</code> or connect to when output is
	 * <code>tcpclient</code>
	 *
	 * @param address
	 *            Hostname or IP address
	 */
	public void setAddress(final String address) {
		setOption(ADDRESS, address);
	}

	/**
	 * Returns the output mode
	 *
	 * @return current output mode
	 */
	public OutputMode getOutput() {
		final String value = options.get(OUTPUT);
		return value == null ? OutputMode.file : OutputMode.valueOf(value);
	}

	/**
	 * Sets the output mode
	 *
	 * @param output
	 *            Output mode
	 */
	public void setOutput(final String output) {
		setOutput(OutputMode.valueOf(output));
	}

	/**
	 * Sets the output mode
	 *
	 * @param output
	 *            Output mode
	 */
	public void setOutput(final OutputMode output) {
		setOption(OUTPUT, output.name());
	}

	/**
	 * Returns the location of the directory where class files should be dumped
	 * to.
	 *
	 * @return dump location or <code>null</code> (no dumps)
	 */
	public String getClassDumpDir() {
		return getOption(CLASSDUMPDIR, null);
	}

	/**
	 * Sets the directory where class files should be dumped to.
	 *
	 * @param location
	 *            dump location or <code>null</code> (no dumps)
	 */
	public void setClassDumpDir(final String location) {
		setOption(CLASSDUMPDIR, location);
	}

	/**
	 * Returns whether the agent exposes functionality via JMX.
	 *
	 * @return <code>true</code>, when JMX is enabled
	 */
	public boolean getJmx() {
		return getOption(JMX, false);
	}

	/**
	 * Sets whether the agent should expose functionality via JMX.
	 *
	 * @param jmx
	 *            <code>true</code> if JMX should be enabled
	 */
	public void setJmx(final boolean jmx) {
		setOption(JMX, jmx);
	}

	private void setOption(final String key, final int value) {
		setOption(key, Integer.toString(value));
	}

	private void setOption(final String key, final boolean value) {
		setOption(key, Boolean.toString(value));
	}

	private void setOption(final String key, final String value) {
		options.put(key, value);
	}

	private String getOption(final String key, final String defaultValue) {
		final String value = options.get(key);
		return value == null ? defaultValue : value;
	}

	private boolean getOption(final String key, final boolean defaultValue) {
		final String value = options.get(key);
		return value == null ? defaultValue : Boolean.parseBoolean(value);
	}

	private int getOption(final String key, final int defaultValue) {
		final String value = options.get(key);
		return value == null ? defaultValue : Integer.parseInt(value);
	}

	/**
	 * Generate required JVM argument based on current configuration and
	 * supplied agent jar location.
	 *
	 * @param agentJarFile
	 *            location of the JaCoCo Agent Jar
	 * @return Argument to pass to create new VM with coverage enabled
	 */
	public String getVMArgument(final File agentJarFile) {
		return format("-javaagent:%s=%s", agentJarFile, this);
	}

	/**
	 * Generate required quoted JVM argument based on current configuration and
	 * supplied agent jar location.
	 *
	 * @param agentJarFile
	 *            location of the JaCoCo Agent Jar
	 * @return Quoted argument to pass to create new VM with coverage enabled
	 */
	public String getQuotedVMArgument(final File agentJarFile) {
		return CommandLineSupport.quote(getVMArgument(agentJarFile));
	}

	/**
	 * Generate required quotes JVM argument based on current configuration and
	 * prepends it to the given argument command line. If a agent with the same
	 * JAR file is already specified this parameter is removed from the existing
	 * command line.
	 *
	 * @param arguments
	 *            existing command line arguments or <code>null</code>
	 * @param agentJarFile
	 *            location of the JaCoCo Agent Jar
	 * @return VM command line arguments prepended with configured JaCoCo agent
	 */
	public String prependVMArguments(final String arguments,
			final File agentJarFile) {
		final List<String> args = CommandLineSupport.split(arguments);
		final String plainAgent = format("-javaagent:%s", agentJarFile);
		for (final Iterator<String> i = args.iterator(); i.hasNext();) {
			if (i.next().startsWith(plainAgent)) {
				i.remove();
			}
		}
		args.add(0, getVMArgument(agentJarFile));
		return CommandLineSupport.quote(args);
	}

	/**
	 * Creates a string representation that can be passed to the agent via the
	 * command line. Might be the empty string, if no options are set.
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final String key : VALID_OPTIONS) {
			final String value = options.get(key);
			if (value != null) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(key).append('=').append(value);
			}
		}
		return sb.toString();
	}

}
