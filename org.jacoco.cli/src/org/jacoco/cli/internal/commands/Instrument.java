/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Keeping - initial implementation
 *    Marc R. Hoffmann - rework
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * The <code>instrument</code> command.
 */
public class Instrument extends Command {

	@Option(name = "--dest", usage = "path to write instrumented Java classes to", metaVar = "<dir>", required = true)
	File dest;

	@Argument(usage = "list of folder or files to instrument recusively", metaVar = "<sourcefiles>")
	List<File> source = new ArrayList<File>();

	private Instrumenter instrumenter;

	@Override
	public String description() {
		return "Off-line instrumentation of Java class files and JAR files.";
	}

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws IOException {
		final File absoluteDest = dest.getAbsoluteFile();
		instrumenter = new Instrumenter(
				new OfflineInstrumentationAccessGenerator());
		int total = 0;
		for (final File s : source) {
			if (s.isFile()) {
				total += instrument(s, new File(absoluteDest, s.getName()));
			} else {
				total += instrumentRecursive(s, absoluteDest);
			}
		}
		out.printf("[INFO] %s classes instrumented to %s.%n",
				Integer.valueOf(total), absoluteDest);
		return 0;
	}

	private int instrumentRecursive(final File src, final File dest)
			throws IOException {
		int total = 0;
		if (src.isDirectory()) {
			for (final File child : src.listFiles()) {
				total += instrumentRecursive(child,
						new File(dest, child.getName()));
			}
		} else {
			total += instrument(src, dest);
		}
		return total;
	}

	private int instrument(final File src, final File dest) throws IOException {
		dest.getParentFile().mkdirs();
		final InputStream input = new FileInputStream(src);
		try {
			final OutputStream output = new FileOutputStream(dest);
			try {
				return instrumenter.instrumentAll(input, output,
						src.getAbsolutePath());
			} finally {
				output.close();
			}
		} catch (final IOException e) {
			dest.delete();
			throw e;
		} finally {
			input.close();
		}
	}

}
