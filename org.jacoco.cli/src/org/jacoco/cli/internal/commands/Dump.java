/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;

import picocli.CommandLine;

/**
 * The <code>dump</code> command.
 */
@CommandLine.Command(name = "dump", description = "Request execution data from a JaCoCo agent running in 'tcpserver' output mode.")
public class Dump extends Command {

	@CommandLine.Option(names = "--address", description = "host name or ip address to connect to (default localhost)", paramLabel = "<address>")
	String address = AgentOptions.DEFAULT_ADDRESS;

	@CommandLine.Option(names = "--port", description = "the port to connect to (default 6300)", paramLabel = "<port>")
	int port = AgentOptions.DEFAULT_PORT;

	@CommandLine.Option(names = "--destfile", description = "file to write execution data to", paramLabel = "<path>", required = true)
	File destfile;

	@CommandLine.Option(names = "--reset", description = "reset execution data on test target after dump")
	boolean reset = false;

	@CommandLine.Option(names = "--retry", description = "number of retries (default 10)", paramLabel = "<count>")
	int retrycount = 10;

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws Exception {
		final ExecDumpClient client = new ExecDumpClient() {
			@Override
			protected void onConnecting(final InetAddress address,
					final int port) {
				out.printf("[INFO] Connecting to %s:%s.%n", address,
						Integer.valueOf(port));
			}

			@Override
			protected void onConnectionFailure(final IOException exception) {
				err.printf("[WARN] %s.%n", exception.getMessage());
			}
		};
		client.setReset(reset);
		client.setRetryCount(retrycount);

		final ExecFileLoader loader = client.dump(address, port);
		out.printf("[INFO] Writing execution data to %s.%n",
				destfile.getAbsolutePath());
		loader.save(destfile, true);

		return 0;
	}

}
