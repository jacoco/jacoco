/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Chas Honton, Marc R. Hoffmann - initial implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;

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
 * @since 0.6.4
 */
@Mojo(name = "dump", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, threadSafe = true)
public class DumpMojo extends AbstractJacocoMojo {

	/**
	 * Path to the output file for execution data.
	 */
	@Parameter(property = "jacoco.destFile", defaultValue = "${project.build.directory}/jacoco.exec")
	private File destFile;

	/**
	 * If set to true and the execution data file already exists, coverage data
	 * is appended to the existing file. If set to false, an existing execution
	 * data file will be replaced.
	 */
	@Parameter(property = "jacoco.append", defaultValue = "true")
	private boolean append;

	/**
	 * Sets whether execution data should be downloaded from the remote host.
	 */
	@Parameter(property = "jacoco.dump", defaultValue = "true")
	private boolean dump;

	/**
	 * Sets whether a reset command should be sent after the execution data has
	 * been dumped.
	 */
	@Parameter(property = "jacoco.reset", defaultValue = "false")
	private boolean reset;

	/**
	 * IP address or hostname to connect to.
	 */
	@Parameter(property = "jacoco.address")
	private String address;

	/**
	 * Port number to connect to. If multiple JaCoCo agents should run on the
	 * same machine, different ports have to be specified for the agents.
	 */
	@Parameter(property = "jacoco.port", defaultValue = "6300")
	private int port;

	/**
	 * Number of retries which the goal will attempt to establish a connection.
	 * This can be used to wait until the target JVM is successfully launched.
	 */
	@Parameter(property = "jacoco.retryCount", defaultValue = "10")
	private int retryCount;

	@Override
	public void executeMojo() throws MojoExecutionException {
		final ExecDumpClient client = new ExecDumpClient() {
			@Override
			protected void onConnecting(final InetAddress address,
					final int port) {
				getLog().info(format("Connecting to %s:%s", address,
						Integer.valueOf(port)));
			}

			@Override
			protected void onConnectionFailure(final IOException exception) {
				getLog().info(exception.getMessage());
			}
		};
		client.setDump(dump);
		client.setReset(reset);
		client.setRetryCount(retryCount);

		try {
			final ExecFileLoader loader = client.dump(address, port);
			if (dump) {
				getLog().info(format("Dumping execution data to %s",
						destFile.getAbsolutePath()));
				loader.save(destFile, append);
			}
		} catch (final IOException e) {
			throw new MojoExecutionException("Unable to dump coverage data", e);
		}
	}

}
