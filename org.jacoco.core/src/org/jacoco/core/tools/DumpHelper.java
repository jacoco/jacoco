/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.tools;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import static java.lang.String.format;
import java.net.InetAddress;
import java.net.Socket;
import org.jacoco.core.data.ExecFileLoader;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Shared loggingic between {@link DumpTask} and {@link DumpMojo}.
 *
 * @author Mirko Friedenhagen
 */
public abstract class DumpHelper {
	/**
	 * IP address or hostname to connect to.
	 */
	private final String address;
	/**
	 * Port number to connect to. If multiple JaCoCo agents should run on the
	 * same machine, different ports have to be specified for the agents.
	 */
	private final int port;
	/**
	 * Path to the output file for execution data.
	 */
	private final File destFile;
	/**
	 * Number of retries which the goal will attempt to establish a connection.
	 * This can be used to wait until the target JVM is successfully launched.
	 */
	private final int retryCount;
	/**
	 * Sets whether execution data should be downloaded from the remote host.
	 */
	private final boolean dump;
	/**
	 * Sets whether a reset command should be sent after the execution data has
	 * been dumped.
	 */
	private final boolean reset;
	/**
	 * If set to true and the execution data file already exists, coverage data
	 * is appended to the existing file. If set to false, an existing execution
	 * data file will be replaced.
	 */
	private final boolean append;


	/**
	 * Constructor.
	 * @param address to connect to.
	 * @param port of connect to.
	 * @param destFile where to write the results.
	 * @param retryCount how often should retry the connect.
	 * @param dump should we dump.
	 * @param reset reset the file.
	 * @param append to existing file.
	 */
	public DumpHelper(String address, int port, File destFile, int retryCount, boolean dump, boolean reset, boolean append) {
		this.address = address;
		this.port = port;
		this.destFile = destFile;
		this.retryCount = retryCount;
		this.dump = dump;
		this.reset = reset;
		this.append = append;

	}

	/**
	 * Dump the results.
	 *
	 * @throws IOException  when we could not connect or there is an error during
	 *                      writing or reading from the socket.
	 */
	public final void execute() throws IOException {
		final ExecFileLoader loader = new ExecFileLoader();

		final Socket socket = tryConnect();
		try {
			// 1. Get streams from socket
			final RemoteControlWriter remoteWriter = new RemoteControlWriter(
					socket.getOutputStream());
			final RemoteControlReader remoteReader = new RemoteControlReader(
					socket.getInputStream());
			remoteReader
					.setSessionInfoVisitor(loader.getSessionInfoStore());
			remoteReader.setExecutionDataVisitor(loader
					.getExecutionDataStore());

			// 2. Request dump
			remoteWriter.visitDumpCommand(dump, reset);
			remoteReader.read();

		} finally {
			socket.close();
		}

		// 3. Write execution data to file
		if (dump) {
			logging(format("Dumping execution data to %s",
					destFile.getAbsolutePath()));
			loader.save(destFile, append);
		}

	}

	private Socket tryConnect() throws IOException {

		final InetAddress inetAddress = InetAddress.getByName(address);
		int count = 0;
		while (true) {
			try {
				logging(format("Connecting to %s:%s", inetAddress,
						Integer.valueOf(port)));
				return new Socket(inetAddress, port);
			} catch (final IOException e) {
				if (++count > retryCount) {
					throw e;
				}
				logging(e.getMessage());
				sleep();
			}
		}
	}

	private void sleep() throws InterruptedIOException {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException ie) {
			throw new InterruptedIOException();
		}
	}

	/**
	 * Logs a message. Logging depends on the used Build framework, i.e. Maven
	 * or Ant.
	 *
	 * @param message
	 */
	protected abstract void logging(String message);

}
