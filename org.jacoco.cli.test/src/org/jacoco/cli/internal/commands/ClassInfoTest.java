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

import org.jacoco.cli.internal.CommandTestBase;
import org.junit.Test;

/**
 * Unit tests for {@link ClassInfo}.
 */
public class ClassInfoTest extends CommandTestBase {

	@Test
	public void should_print_usage_when_invalid_option_is_given()
			throws Exception {
		execute("classinfo", "--invalid");

		assertFailure();
		assertContains("\"--invalid\"", err);
		assertContains(
				"java -jar jacococli.jar classinfo [<classlocations> ...]",
				err);
	}

	@Test
	public void should_print_warning_when_no_class_files_are_provided()
			throws Exception {
		execute("classinfo");

		assertOk();
		assertContains("[WARN] No class files provided.", out);
	}

	@Test
	public void should_print_class_info() throws Exception {
		execute("classinfo", getClassPath());

		assertOk();
		assertContains("class", out);
		assertContains("org/jacoco/cli/internal/commands/ClassInfoTest", out);
		assertContainsNot("method", out);
	}

	@Test
	public void should_print_class_details_when_verbose() throws Exception {
		execute("classinfo", "--verbose", getClassPath());

		assertOk();
		assertContains("line", out);
		assertContains("method", out);
		assertContains("line", out);
	}

}
