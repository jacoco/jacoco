/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Chas Honton, Marc R. Hoffmann - initial implementation
 *
 *******************************************************************************/
package org.jacoco.maven;


import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.jacoco.core.tools.DumpHelper;

/**
 * <p>
 * Request a dump over TCP/IP from a JaCoCo agent running in
 * <code>tcpserver</code> mode.
 * </p>
 * 
 * <p>
 * Note concerning parallel builds: While the dump goal as such is thread safe,
 * it has to be considered that TCP/IP server ports of the agents are a shared
 * resource.
 * </p>
 * 
 * @goal dump
 * @phase post-integration-test
 * @threadSafe
 * @since 0.6.4
 */
public class DumpMojo extends AbstractJacocoMojo {

	/**
	 * Path to the output file for execution data.
	 * 
	 * @parameter expression="${jacoco.destFile}"
	 *            default-value="${project.build.directory}/jacoco.exec"
	 */
	private File destFile;

	/**
	 * If set to true and the execution data file already exists, coverage data
	 * is appended to the existing file. If set to false, an existing execution
	 * data file will be replaced.
	 * 
	 * @parameter expression="${jacoco.append}" default-value="true"
	 */
	private boolean append;

	/**
	 * Sets whether execution data should be downloaded from the remote host.
	 * 
	 * @parameter expression="${jacoco.dump}" default-value="true"
	 */
	private boolean dump;

	/**
	 * Sets whether a reset command should be sent after the execution data has
	 * been dumped.
	 * 
	 * @parameter expression="${jacoco.reset}" default-value="false"
	 */
	private boolean reset;

	/**
	 * IP address or hostname to connect to.
	 * 
	 * @parameter expression="${jacoco.address}"
	 */
	private String address;

	/**
	 * Port number to connect to. If multiple JaCoCo agents should run on the
	 * same machine, different ports have to be specified for the agents.
	 * 
	 * @parameter expression="${jacoco.port}" default-value="6300"
	 */
	private int port;

	/**
	 * Number of retries which the goal will attempt to establish a connection.
	 * This can be used to wait until the target JVM is successfully launched.
	 * 
	 * @parameter expression="${jacoco.retryCount}" default-value="10"
	 */
	private int retryCount;

	@Override
	public void executeMojo() throws MojoExecutionException {
		final DumpHelper dumpHelper = new DumpHelper(
				address, port, destFile, retryCount, dump, reset, append) {
			@Override
			protected void logging(String format) {
				getLog().info(format);
			}
		};
		try {
			dumpHelper.execute();
		} catch (final IOException e) {
			throw new MojoExecutionException("Unable to dump coverage data", e);
		}
	}
}