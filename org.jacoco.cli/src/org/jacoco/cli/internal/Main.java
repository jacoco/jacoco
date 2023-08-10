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
package org.jacoco.cli.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;

/**
 * Entry point for all command line operations.
 */
public class Main extends Command {

	private static final PrintWriter NUL = new PrintWriter(new Writer() {

		@Override
		public void write(final char[] arg0, final int arg1, final int arg2)
				throws IOException {
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void close() throws IOException {
		}
	});

	private final String[] args;

	Main(final String... args) {
		this.args = args;
	}

	@Argument(handler = CommandHandler.class, required = true)
	Command command;

	@Override
	public String description() {
		return "Command line interface for JaCoCo.";
	}

	@Override
	public String usage(final CommandParser parser) {
		return JAVACMD + "--help | <command>";
	}

	@Override
	public int execute(PrintWriter out, final PrintWriter err)
			throws Exception {

		final CommandParser mainParser = new CommandParser(this);
		try {
			mainParser.parseArgument(args);
		} catch (final CmdLineException e) {
			((CommandParser) e.getParser()).getCommand().printHelp(err);
			err.println();
			err.println(e.getMessage());
			return -1;
		}

		if (help) {
			printHelp(out);
			return 0;
		}

		if (command.help) {
			command.printHelp(out);
			return 0;
		}

		if (command.quiet) {
			out = NUL;
		}

		return command.execute(out, err);
	}

	/**
	 * Main entry point for program invocations.
	 *
	 * @param args
	 *            program arguments
	 * @throws Exception
	 *             All internal exceptions are directly passed on to get printed
	 *             on the console
	 */
	public static void main(final String... args) throws Exception {
		final PrintWriter out = new PrintWriter(System.out, true);
		final PrintWriter err = new PrintWriter(System.err, true);
		final int returncode = new Main(args).execute(out, err);
		System.exit(returncode);
	}

}
