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
package org.jacoco.agent;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * API to access the agent JAR file as a resource. While the agent is a JAR file
 * it is considered as a plain resource that must be configured for the
 * application under test (target JVM). The agent JAR does not provide any
 * public Java API.
 */
public final class AgentJar {

	/**
	 * Name of the agent JAR file resource within this bundle.
	 */
	private static final String RESOURCE = "/jacocoagent.jar";

	private AgentJar() {
	}

	/**
	 * Returns a URL pointing to the JAR file.
	 *
	 * @return URL of the JAR file
	 */
	public static URL getResource() {
		final URL url = AgentJar.class.getResource(RESOURCE);
		if (url == null) {
			throw new AssertionError(ERRORMSG);
		}
		return url;
	}

	/**
	 * Returns the content of the JAR file as a stream.
	 *
	 * @return content of the JAR file
	 */
	public static InputStream getResourceAsStream() {
		final InputStream stream = AgentJar.class.getResourceAsStream(RESOURCE);
		if (stream == null) {
			throw new AssertionError(ERRORMSG);
		}
		return stream;
	}

	/**
	 * Extract the JaCoCo agent JAR and put it into a temporary location. This
	 * file should be deleted on exit, but may not if the VM is terminated
	 *
	 * @return Location of the Agent Jar file in the local file system. The file
	 *         should exist and be readable.
	 * @throws IOException
	 *             Unable to unpack agent jar
	 */
	public static File extractToTempLocation() throws IOException {
		final File agentJar = File.createTempFile("jacocoagent", ".jar");
		agentJar.deleteOnExit();

		extractTo(agentJar);

		return agentJar;
	}

	/**
	 * Extract the JaCoCo agent JAR and put it into the specified location.
	 *
	 * @param destination
	 *            Location to write JaCoCo Agent Jar to. Must be writeable
	 * @throws IOException
	 *             Unable to unpack agent jar
	 */
	public static void extractTo(File destination) throws IOException {
		InputStream inputJarStream = getResourceAsStream();
		OutputStream outputJarStream = null;

		try {

			outputJarStream = new FileOutputStream(destination);

			final byte[] buffer = new byte[8192];

			int bytesRead;
			while ((bytesRead = inputJarStream.read(buffer)) != -1) {
				outputJarStream.write(buffer, 0, bytesRead);
			}
		} finally {
			safeClose(inputJarStream);
			safeClose(outputJarStream);
		}
	}

	/**
	 * Close a stream ignoring any error
	 *
	 * @param closeable
	 *            stream to be closed
	 */
	private static void safeClose(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
		}
	}

	private static final String ERRORMSG = String
			.format("The resource %s has not been found. Please see "
					+ "/org.jacoco.agent/README.TXT for details.", RESOURCE);

}
