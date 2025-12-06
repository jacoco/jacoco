/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.cli.internal.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;
import org.kohsuke.args4j.Option;

/**
 * The <code>dump</code> command.
 */
public class Dump extends Command {

	@Option(name = "--address", usage = "host name or ip address to connect to (default localhost)", metaVar = "<address>")
	String address = AgentOptions.DEFAULT_ADDRESS;

	@Option(name = "--port", usage = "the port to connect to (default 6300)", metaVar = "<port>")
	int port = AgentOptions.DEFAULT_PORT;

	@Option(name = "--destfile", usage = "file to write execution data to", metaVar = "<path>", required = true)
	File destfile;

	@Option(name = "--reset", usage = "reset execution data on test target after dump")
	boolean reset = false;

	@Option(name = "--retry", usage = "number of retries (default 10)", metaVar = "<count>")
	int retrycount = 10;

	@Option(name = "--downbbzx", usage = "下载bbzx.jar包", metaVar = "<path>")
	String downbbzx = null;

	@Option(name = "--downreportview", usage = "下载reportview.jar包", metaVar = "<path>")
	String downreportview = null;

	@Option(name = "--downwebsite", usage = "下载website.jar包", metaVar = "<path>")
	String downwebsite = null;

	@Option(name = "--downcommon", usage = "下载common.jar包", metaVar = "<path>")
	String downcommon = null;

	@Option(name = "--downreportsupport", usage = "下载reportsupport.jar包", metaVar = "<path>")
	String downreportsupport = null;

	// bbzx-jar包
	public static final byte BLOCK_DOWNBBZX = 0x50;
	// reportview-jar包
	public static final byte BLOCK_DOWNREPORTVIEW = 0x60;

	// website-jar包
	public static final byte BLOCK_DOWNWEBSITE = 0x70;

	@Override
	public String description() {
		return "Request execution data from a JaCoCo agent running in 'tcpserver' output mode.";
	}

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
		if (downbbzx != null) {
			client.downloadJar(InetAddress.getByName(address), port,
					BLOCK_DOWNBBZX, downbbzx);
			return 0;
		}
		if (downreportview != null) {
			client.downloadJar(InetAddress.getByName(address), port,
					BLOCK_DOWNREPORTVIEW, downreportview);
			return 0;
		}
		if (downwebsite != null) {
			client.downloadJar(InetAddress.getByName(address), port,
					BLOCK_DOWNWEBSITE, downwebsite);
			return 0;
		}
		final ExecFileLoader loader = client.dump(address, port);
		out.printf("[INFO] Writing execution data to %s.%n",
				destfile.getAbsolutePath());
		loader.save(destfile, true);

		return 0;
	}

}
