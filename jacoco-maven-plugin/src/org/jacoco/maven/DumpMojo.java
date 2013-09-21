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

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.maven.plugin.MojoExecutionException;
import org.jacoco.core.data.ExecFileLoader;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Request a dump over TCP/IP from a JaCoCo agent running in
 * <code>tcpserver</code> mode.
 * 
 * @goal dump
 * 
 * @phase post-integration-test
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

	@Override
	public void executeMojo() throws MojoExecutionException {
		try {
			final ExecFileLoader loader = new ExecFileLoader();

			// 1. Open socket connection
			final Socket socket = new Socket(InetAddress.getByName(address),
					port);
			getLog().info(
					format("Connecting to %s", socket.getRemoteSocketAddress()));
			final RemoteControlWriter remoteWriter = new RemoteControlWriter(
					socket.getOutputStream());
			final RemoteControlReader remoteReader = new RemoteControlReader(
					socket.getInputStream());
			remoteReader.setSessionInfoVisitor(loader.getSessionInfoStore());
			remoteReader
					.setExecutionDataVisitor(loader.getExecutionDataStore());

			// 2. Request dump
			remoteWriter.visitDumpCommand(dump, reset);
			remoteReader.read();

			// 3. Write execution data to file
			if (dump) {
				getLog().info(
						format("Dumping execution data to %s",
								destFile.getAbsolutePath()));
				loader.save(destFile, append);
			}

			socket.close();

		} catch (final IOException e) {
			throw new MojoExecutionException("Unable to dump coverage data", e);
		}
	}

}
