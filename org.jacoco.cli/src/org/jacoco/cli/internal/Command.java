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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.kohsuke.args4j.Option;

/**
 * Common interface for all commands.
 */
public abstract class Command {

	/**
	 * Common command line prefix.
	 */
	public static final String JAVACMD = "java -jar jacococli.jar ";

	/**
	 * Flag whether help should be printed for this command.
	 */
	@Option(name = "--help", usage = "show help", help = true)
	public boolean help = false;

	/**
	 * Flag whether output to stdout should be suppressed.
	 */
	@Option(name = "--quiet", usage = "suppress all output on stdout")
	public boolean quiet = false;

	/**
	 * @return Short description of the command.
	 */
	public abstract String description();

	/**
	 * @return name of the command
	 */
	public String name() {
		return getClass().getSimpleName().toLowerCase();
	}

	/**
	 * @param parser
	 *            parser for this command
	 * @return usage string displayed for help
	 */
	public String usage(final CommandParser parser) {
		final StringWriter writer = new StringWriter();
		parser.printSingleLineUsage(writer, null);
		return JAVACMD + name() + writer;
	}

	/**
	 * Executes the given command.
	 *
	 * @param out
	 *            std out
	 * @param err
	 *            std err
	 * @return exit code, should be 0 for normal operation
	 * @throws Exception
	 *             any exception that my occur during execution
	 */
	public abstract int execute(PrintWriter out, PrintWriter err)
			throws Exception;

	/**
	 * Prints textual help for this command.
	 *
	 * @param writer
	 *            output destination
	 */
	protected void printHelp(final PrintWriter writer) {
		final CommandParser parser = new CommandParser(this);
		writer.println(description());
		writer.println();
		writer.println("Usage: " + parser.getCommand().usage(parser));
		parser.printUsage(writer, null);
	}

}
