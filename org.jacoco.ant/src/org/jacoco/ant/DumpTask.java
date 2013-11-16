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
package org.jacoco.ant;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jacoco.core.data.ExecFileLoader;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Ant task for remotely controlling an application that is running with the
 * tcpserver output mode
 */
public class DumpTask extends Task {

	private boolean dump = true;
	private boolean reset = false;
	private File destfile = null;
	private String address = AgentOptions.DEFAULT_ADDRESS;
	private int port = AgentOptions.DEFAULT_PORT;
	private int retryCount = 10;
	private boolean append = true;

	/**
	 * Sets the location of the execution data file to write. This parameter is
	 * required when dump is <code>true</code>. Default is
	 * <code>jacoco.exec</code>
	 * 
	 * @param destfile
	 *            Location to write execution data to
	 */
	public void setDestfile(final File destfile) {
		this.destfile = destfile;
	}

	/**
	 * IP Address or hostname to connect to. Defaults to <code>localhost</code>
	 * 
	 * @param address
	 *            IP Address or hostname to connect to
	 */
	public void setAddress(final String address) {
		this.address = address;
	}

	/**
	 * Port number to connect to. Default is <code>6300</code>
	 * 
	 * @param port
	 *            Port to connect to
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	/**
	 * Number of retries which the goal will attempt to establish a connection.
	 * This can be used to wait until the target JVM is successfully launched.
	 * 
	 * @param retryCount
	 *            number of retries
	 */
	public void setRetryCount(final int retryCount) {
		this.retryCount = retryCount;
	}

	/**
	 * <code>true</code> if the destination file it to be appended to.
	 * <code>false</code> if the file is to be overwritten
	 * 
	 * @param append
	 *            <code>true</code> if the destination file should be appended
	 *            to
	 */
	public void setAppend(final boolean append) {
		this.append = append;
	}

	/**
	 * Sets whether execution data should be downloaded from the remote host.
	 * Defaults to <code>true</code>
	 * 
	 * @param dump
	 *            <code>true</code> to download execution data
	 */
	public void setDump(final boolean dump) {
		this.dump = dump;
	}

	/**
	 * Sets whether a reset command should be sent after the execution data has
	 * been dumped. Defaults to <code>false</code>
	 * 
	 * @param reset
	 *            <code>true</code> to reset execution data
	 */
	public void setReset(final boolean reset) {
		this.reset = reset;
	}

	@Override
	public void execute() throws BuildException {

		if (port <= 0) {
			throw new BuildException("Invalid port value", getLocation());
		}
		if (dump && destfile == null) {
			throw new BuildException(
					"Destination file is required when dumping execution data",
					getLocation());
		}

		try {

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
				log(format("Dumping execution data to %s",
						destfile.getAbsolutePath()));
				loader.save(destfile, append);
			}


		} catch (final IOException e) {
			throw new BuildException("Unable to dump coverage data", e,
					getLocation());
		}
	}

	private Socket tryConnect() throws IOException {
		final InetAddress inetAddress = InetAddress.getByName(address);
		int count = 0;
		while (true) {
			try {
				log(format("Connecting to %s:%s", inetAddress,
						Integer.valueOf(port)));
				return new Socket(inetAddress, port);
			} catch (final IOException e) {
				if (++count > retryCount) {
					throw e;
				}
				log(e.getMessage());
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

}
