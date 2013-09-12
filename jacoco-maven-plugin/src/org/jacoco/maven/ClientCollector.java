/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Chas Honton - initial implementation
 *
 *******************************************************************************/

package org.jacoco.maven;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.maven.plugin.MojoExecutionException;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * 
 * A tcpclient dump collector
 * 
 */
public class ClientCollector extends AbstractCollector {

	ClientCollector(final AgentOptions options) {
		super(options);
	}

	void dump() throws IOException, MojoExecutionException {

		final Socket socket = createClientSocket();

		final RemoteControlWriter remoteWriter = new RemoteControlWriter(
				socket.getOutputStream());
		final RemoteControlReader remoteReader = new RemoteControlReader(
				socket.getInputStream());

		final OutputStream output = createFileOutputStream();
		try {
			final ExecutionDataWriter outputWriter = new ExecutionDataWriter(
					output);
			remoteReader.setSessionInfoVisitor(outputWriter);
			remoteReader.setExecutionDataVisitor(outputWriter);

			remoteWriter.visitDumpCommand(true, false);
			remoteReader.read();

			socket.close();
		} finally {
			output.close();
		}
	}

	private Socket createClientSocket() throws MojoExecutionException {
		final InetSocketAddress endpoint = getSocketAddress(true);
		try {
			final Socket client = new Socket();
			client.connect(endpoint);
			return client;
		} catch (final IOException e) {
			throw new MojoExecutionException("Can not open client socket "
					+ endpoint, e);
		}
	}

}
