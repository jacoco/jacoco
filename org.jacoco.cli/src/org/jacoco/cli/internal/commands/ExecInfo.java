/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.kohsuke.args4j.Argument;

/**
 * The <code>execinfo</code> command.
 */
public class ExecInfo extends Command {

	@Argument(usage = "list of JaCoCo *.exec files to read", metaVar = "<execfiles>")
	List<File> execfiles = new ArrayList<File>();

	@Override
	public String description() {
		return "Print exec file content in human readable format.";
	}

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws IOException {
		if (execfiles.isEmpty()) {
			out.println("[WARN] No execution data files provided.");
		} else {
			for (final File file : execfiles) {
				dump(file, out);
			}
		}
		return 0;
	}

	private void dump(final File file, final PrintWriter out)
			throws IOException {
		out.printf("[INFO] Loading exec file %s.%n", file);
		out.println("CLASS ID         HITS/PROBES   CLASS NAME");

		final FileInputStream in = new FileInputStream(file);
		final ExecutionDataReader reader = new ExecutionDataReader(in);
		reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
			public void visitSessionInfo(final SessionInfo info) {
				out.printf("Session \"%s\": %s - %s%n", info.getId(),
						new Date(info.getStartTimeStamp()),
						new Date(info.getDumpTimeStamp()));
			}
		});
		reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
			public void visitClassExecution(final ExecutionData data) {
				out.printf("%016x  %3d of %3d   %s%n",
						Long.valueOf(data.getId()),
						Integer.valueOf(getHitCount(data.getProbes())),
						Integer.valueOf(data.getProbes().length),
						data.getName());
			}
		});
		reader.read();
		in.close();
		out.println();
	}

	private int getHitCount(final boolean[] data) {
		int count = 0;
		for (final boolean hit : data) {
			if (hit) {
				count++;
			}
		}
		return count;
	}

}
