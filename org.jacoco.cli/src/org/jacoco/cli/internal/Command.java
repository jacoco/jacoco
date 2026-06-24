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
import java.io.Writer;
import java.util.concurrent.Callable;

import picocli.CommandLine;

/**
 * Common interface for all commands.
 */
public abstract class Command implements Callable<Integer> {

	@CommandLine.Spec
	CommandLine.Model.CommandSpec commandSpec;

	/**
	 * Flag whether help should be printed for this command.
	 */
	@CommandLine.Option(names = "--help", description = "show help", usageHelp = true)
	public boolean help = false;

	/**
	 * Flag whether output to stdout should be suppressed.
	 */
	@CommandLine.Option(names = "--quiet", description = "suppress all output on stdout")
	public boolean quiet = false;

	/**
	 * Executes this command with given {@code out} and {@code err}.
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
	 * Executes this command.
	 *
	 * @return exit code, should be 0 for normal operation
	 * @throws Exception
	 *             any exception that my occur during execution
	 */
	public final Integer call() throws Exception {
		return execute(quiet ? NUL : commandSpec.commandLine().getOut(),
				commandSpec.commandLine().getErr());
	}

	private static final PrintWriter NUL = new PrintWriter(new Writer() {
		@Override
		public void write(final char[] arg0, final int arg1, final int arg2) {
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() {
		}
	});

}
