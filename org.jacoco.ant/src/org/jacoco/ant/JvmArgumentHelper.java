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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.jacoco.core.runtime.AgentOptions;

/**
 * Helper class to generate the JVM argument required to start a new JVM with a
 * code coverage agent
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
class JvmArgumentHelper {
	private final File agentJar;

	JvmArgumentHelper() {
		final InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("jacocoagent.jar");
		try {
			agentJar = extractAgentJar(inputStream);
		} finally {
			FileUtils.close(inputStream);
		}
	}

	/**
	 * Extract the JaCoCo agent jar from the classpath and put it into a
	 * temporary location.
	 * 
	 * @param inputJarStream
	 *            Open stream pointing to the JaCoCo jar
	 * @return Local physical location of the JaCoCo agent jar. This file will
	 *         be removed once the task has been executed
	 */
	private File extractAgentJar(final InputStream inputJarStream) {

		if (inputJarStream == null) {
			throw new BuildException("Unable to locate Agent Jar");
		}

		OutputStream outputJarStream = null;
		try {
			final File agentJar = File.createTempFile("jacocoagent", ".jar");
			agentJar.deleteOnExit();

			outputJarStream = new FileOutputStream(agentJar);

			final byte[] buffer = new byte[8192];

			int bytesRead;
			while ((bytesRead = inputJarStream.read(buffer)) != -1) {
				outputJarStream.write(buffer, 0, bytesRead);
			}

			return agentJar;
		} catch (final IOException e) {
			throw new BuildException("Unable to unpack Agent Jar", e);
		} finally {
			FileUtils.close(outputJarStream);
		}
	}

	/**
	 * Generate required JVM argument string based on current configuration and
	 * agent jar location
	 * 
	 * @return Argument to pass to create new VM with coverage enabled
	 */
	String createJavaAgentParam(final AgentOptions agentOptions) {
		final StringBuilder param = new StringBuilder();
		param.append("-javaagent:");
		param.append(agentJar.getAbsolutePath());
		param.append("=");
		param.append(agentOptions.toString());

		return param.toString();
	}
}
