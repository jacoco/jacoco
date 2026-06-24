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

import org.junit.Test;

/**
 * Unit tests for {@link Main}.
 */
public class MainTest extends CommandTestBase {

	@Test
	public void should_print_usage_when_no_arguments_given() throws Exception {
		execute();

		assertFailure();
		assertNoOutput(out);
		assertContains("Missing required subcommand", err);
		assertContains("Usage: java -jar jacococli.jar [COMMAND]", err);
		assertContains("Command line interface for JaCoCo.", err);
	}

	@Test
	public void should_print_error_message_when_invalid_command_is_given()
			throws Exception {
		execute("foo");

		assertFailure();
		assertNoOutput(out);
		assertContains("Unmatched argument at index 0: 'foo'", err);
		assertContains("Usage: java -jar jacococli.jar [COMMAND]", err);
	}

	@Test
	public void should_print_error_message_when_invalid_option_is_given()
			throws Exception {
		execute("--invalid");

		assertFailure();
		assertNoOutput(out);
		assertContains("Unknown option: '--invalid'", err);
		assertContains("Usage: java -jar jacococli.jar [COMMAND]", err);
	}

	@Test
	public void should_print_general_usage_when_help_option_is_given()
			throws Exception {
		execute("--help");

		assertOk();
		assertNoOutput(err);
		assertContains("Usage: java -jar jacococli.jar [COMMAND]", out);
		assertContains("Commands:", out);
		assertContains(" instrument ", out);
	}

	@Test
	public void should_print_command_usage_when_command_and_help_option_is_given()
			throws Exception {
		execute("dump", "--help");

		assertOk();
		assertNoOutput(err);
		assertContains("Usage: java -jar jacococli.jar dump", out);
		assertContains(
				"Request execution data from a JaCoCo agent running in 'tcpserver' output mode.",
				out);
	}

	@Test
	public void should_not_print_any_output_when_quiet_option_is_given()
			throws Exception {
		execute("version", "--quiet");

		assertOk();
		assertNoOutput(out);
		assertNoOutput(err);
	}

	@Test
	public void wip() throws Exception {
		// TODO actually behavior is unchanged
		execute("--quiet", "version");

		assertOk();
		assertNoOutput(out);
		assertNoOutput(err);
	}

	@Test
	public void wip2() throws Exception {
		// TODO actually behavior is unchanged
		execute("--help", "version");

		assertOk();
		assertNoOutput(out);
		assertNoOutput(err);
	}

	@Test
	public void wip3() throws Exception {
		// TODO setOverwrittenOptionsAllowed ?
		execute("version", "--quiet", "--quiet");

		assertOk();
		assertNoOutput(out);
		assertNoOutput(err);
	}

}
