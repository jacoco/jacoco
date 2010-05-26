/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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
import java.net.InetAddress;
import java.net.Socket;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Ant task for remotely controlling an application that is running with the
 * tcpserver output mode
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class DumpTask extends Task {
	private final AgentOptions options = new AgentOptions();
	private boolean dump = true;
	private boolean reset;

	/**
	 * Sets the location of the execution data file to write. Default is
	 * <code>jacoco.exec</code>
	 * 
	 * @param destfile
	 *            Location to write execution data to
	 */
	public void setDestFile(final File destfile) {
		options.setDestfile(destfile.getAbsolutePath());
	}

	/**
	 * IP Address or hostname to connect to. Defaults to <code>localhost</code>
	 * 
	 * @param address
	 *            IP Address or hostname to connect to
	 */
	public void setAddress(final String address) {
		options.setAddress(address);
	}

	/**
	 * Port number to connect to. Default is <code>6300</code>
	 * 
	 * @param port
	 *            Port to connect to
	 */
	public void setPort(final int port) {
		options.setPort(port);
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
		options.setAppend(append);
	}

	/**
	 * Sets whether execution data should be downloaded from the remote host.
	 * Defaults to <code>false</code>
	 * 
	 * @param dump
	 *            <code>true</code> to download execution data
	 */
	public void setDump(final boolean dump) {
		this.dump = dump;
	}

	/**
	 * Sets whether a reset command should be sent after the execution data has
	 * been copied. Defaults to <code>false</code>
	 * 
	 * @param reset
	 *            <code>true</code> to reset execution data
	 */
	public void setReset(final boolean reset) {
		this.reset = reset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {

		final File destFile = new File(options.getDestfile());
		FileOutputStream fileOutput = null;
		ExecutionDataWriter fileWriter;
		try {
			fileOutput = new FileOutputStream(destFile, options.getAppend());
			fileWriter = new ExecutionDataWriter(fileOutput);
		} catch (final IOException e) {
			throw new BuildException("Unable to create destination file", e);
		}

		try {
			final Socket socket = new Socket(InetAddress.getByName(options
					.getAddress()), options.getPort());
			final RemoteControlWriter remoteWriter = new RemoteControlWriter(
					socket.getOutputStream());
			final RemoteControlReader remoteReader = new RemoteControlReader(
					socket.getInputStream());
			remoteReader.setSessionInfoVisitor(fileWriter);
			remoteReader.setExecutionDataVisitor(fileWriter);

			remoteWriter.visitDumpCommand(dump, reset);
			remoteReader.read();

			socket.close();
		} catch (final IOException e) {
			throw new BuildException(
					"Unable to communicate with JaCoCo server", e);
		} finally {
			FileUtils.close(fileOutput);
		}
	}
}
