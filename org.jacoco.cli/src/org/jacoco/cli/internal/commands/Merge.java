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
import java.util.ArrayList;
import java.util.List;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.tools.ExecFileLoader;

import picocli.CommandLine;

/**
 * The <code>merge</code> command.
 */
@CommandLine.Command(name = "merge", description = "Merges multiple exec files into a new one.")
public class Merge extends Command {

	@CommandLine.Parameters(description = "list of JaCoCo *.exec files to read", paramLabel = "<execfiles>")
	List<File> execfiles = new ArrayList<File>();

	@CommandLine.Option(names = "--destfile", description = "file to write merged execution data to", paramLabel = "<path>", required = true)
	File destfile;

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws IOException {
		final ExecFileLoader loader = loadExecutionData(out);
		out.printf("[INFO] Writing execution data to %s.%n",
				destfile.getAbsolutePath());
		loader.save(destfile, true);
		return 0;
	}

	private ExecFileLoader loadExecutionData(final PrintWriter out)
			throws IOException {
		final ExecFileLoader loader = new ExecFileLoader();
		if (execfiles.isEmpty()) {
			out.println("[WARN] No execution data files provided.");
		} else {
			for (final File file : execfiles) {
				out.printf("[INFO] Loading execution data file %s.%n",
						file.getAbsolutePath());
				loader.load(file);
			}
		}
		return loader;
	}

}
