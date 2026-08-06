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
package org.jacoco.cli.internal;

import java.io.PrintWriter;

import org.jacoco.cli.internal.commands.ClassInfo;
import org.jacoco.cli.internal.commands.Dump;
import org.jacoco.cli.internal.commands.ExecInfo;
import org.jacoco.cli.internal.commands.Instrument;
import org.jacoco.cli.internal.commands.Merge;
import org.jacoco.cli.internal.commands.Report;
import org.jacoco.cli.internal.commands.Version;

import picocli.CommandLine;

/**
 * Entry point for all command line operations.
 */
@CommandLine.Command( //
		name = "java -jar jacococli.jar", //
		description = "Command line interface for JaCoCo.", //
		subcommands = { //
				Dump.class, //
				Instrument.class, //
				Merge.class, //
				Report.class, //
				ClassInfo.class, //
				ExecInfo.class, //
				Version.class //
		})
public class Main {

	static CommandLine commandLine(final CommandLine.Help.Ansi ansi,
			final PrintWriter out, final PrintWriter err) {
		return new CommandLine(Main.class) //
				.addSubcommand("help", CommandLine.HelpCommand.class, "--help")
				.setColorScheme(CommandLine.Help.defaultColorScheme(ansi))
				.setOut(out) //
				.setErr(err) //
				.setSeparator(" ") //
				.setUsageHelpAutoWidth(true) //
				.setParameterExceptionHandler( //
						new CommandLine.IParameterExceptionHandler() {
							public int handleParseException(
									final CommandLine.ParameterException ex,
									final String[] args) {
								final CommandLine cmd = ex.getCommandLine();
								cmd.usage(cmd.getErr());
								cmd.getErr().println();
								cmd.getErr().println(cmd.getColorScheme()
										.errorText(ex.getMessage()));
								return 2;
							}
						});
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
		final CommandLine commandLine = commandLine(CommandLine.Help.Ansi.AUTO,
				out, err);
		int returncode = commandLine.execute(args);
		System.exit(returncode);
	}

}
