/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal;

import java.io.PrintWriter;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;

/**
 * Entry point for all command line operations.
 */
public class Main extends Command {

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
		return JAVACMD + "-help | <command>";
	}

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws Exception {

		final CommandParser mainParser = new CommandParser(this);
		try {
			mainParser.parseArgument(args);
		} catch (final CmdLineException e) {
			err.println(e.getMessage());
			err.println();
			((CommandParser) e.getParser()).getCommand().printHelp(err);
			return -1;
		}

		if (help) {
			printHelp(out);
			return 0;
		} else if (command.help) {
			command.printHelp(out);
			return 0;
		} else {
			return command.execute(out, err);
		}
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
		new Main(args).execute(new PrintWriter(System.out, true),
				new PrintWriter(System.err, true));
	}

}
