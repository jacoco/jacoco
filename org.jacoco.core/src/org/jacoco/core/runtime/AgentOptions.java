/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.runtime;

import static java.lang.String.format;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to create and parse options for the runtime agent. Options are
 * represented as a string in the following format:
 * 
 * <pre>
 *   key1=value1,key2=value2,key3=value3
 * </pre>
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class AgentOptions {

	/**
	 * Specifies the output file for execution data. Default is
	 * <code>jacoco.exec</code> in the working directory.
	 */
	public static final String DESTFILE = "destfile";

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

	private static final Collection<String> VALID_OPTIONS = Arrays.asList(
			DESTFILE, APPEND, INCLUDES, EXCLUDES, EXCLCLASSLOADER);

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
			for (final String entry : optionstr.split(",")) {
				final int pos = entry.indexOf('=');
				if (pos == -1) {
					throw new IllegalArgumentException(format(
							"Invalid agent option syntax \"%s\".", optionstr));
				}
				final String key = entry.substring(0, pos);
				if (!VALID_OPTIONS.contains(key)) {
					throw new IllegalArgumentException(format(
							"Unknown agent option \"%s\".", key));
				}
				options.put(key, entry.substring(pos + 1));
			}
		}
	}

	/**
	 * Returns the output file location.
	 * 
	 * @return output file location
	 */
	public String getDestfile() {
		final String destfile = options.get(DESTFILE);
		return destfile == null ? "jacoco.exec" : destfile;
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
		final String value = options.get(APPEND);
		return value == null ? true : Boolean.parseBoolean(value);
	}

	/**
	 * Sets whether the output should be appended to an existing file.
	 * 
	 * @param append
	 *            <code>true</code>, when the output should be appended
	 */
	public void setAppend(final boolean append) {
		setOption(APPEND, String.valueOf(append));
	}

	/**
	 * Returns the wildcard expression for classes to include.
	 * 
	 * @return wildcard expression for classes to include
	 * @see WildcardMatcher
	 */
	public String getIncludes() {
		final String value = options.get(INCLUDES);
		return value == null ? "*" : value;
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
		final String value = options.get(EXCLUDES);
		return value == null ? "" : value;
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
		final String value = options.get(EXCLCLASSLOADER);
		return value == null ? "sun.reflect.DelegatingClassLoader" : value;
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

	private void setOption(final String key, final String value) {
		if (value.contains(",")) {
			throw new IllegalArgumentException(format(
					"Invalid character in option argument \"%s\"", value));
		}
		options.put(key, value);
	}

	/**
	 * Generate required JVM argument string based on current configuration and
	 * supplied agent jar location
	 * 
	 * @param agentJarFile
	 *            location of the JaCoCo Agent Jar
	 * @return Argument to pass to create new VM with coverage enabled
	 */
	public String getVMArgument(final File agentJarFile) {
		final StringBuilder param = new StringBuilder();
		param.append('"');
		param.append("-javaagent:");
		param.append(agentJarFile.toString());
		param.append("=");
		param.append(this.toString());
		param.append('"');

		return param.toString();
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
