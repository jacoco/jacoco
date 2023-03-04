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
import java.io.FileOutputStream;
import java.io.IOException;

import org.jacoco.cli.internal.CommandTestBase;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link ExecInfo}.
 */
public class ExecInfoTest extends CommandTestBase {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void should_print_usage_when_invalid_argument_is_given()
			throws Exception {
		execute("execinfo", "--invalid");

		assertFailure();
		assertContains("\"--invalid\"", err);
		assertContains("java -jar jacococli.jar execinfo [<execfiles> ...]",
				err);
	}

	@Test
	public void should_print_warning_when_no_exec_files_are_provided()
			throws Exception {
		execute("execinfo");

		assertOk();
		assertContains("[WARN] No execution data files provided.", out);
	}

	@Test
	public void should_print_execution_data_info() throws Exception {
		File execfile = createExecFile();

		execute("execinfo", execfile.getAbsolutePath());

		assertOk();
		assertContains("[INFO] Loading exec file " + execfile.getAbsolutePath(),
				out);
		assertContains("CLASS ID         HITS/PROBES   CLASS NAME", out);
		assertContains("Session \"testid\":", out);
		assertContains("0000000000001234    2 of   3   foo/MyClass", out);
	}

	private File createExecFile() throws IOException {
		File f = new File(tmp.getRoot(), "test.exec");
		final FileOutputStream out = new FileOutputStream(f);
		final ExecutionDataWriter writer = new ExecutionDataWriter(out);
		writer.visitSessionInfo(new SessionInfo("testid", 1, 2));
		writer.visitClassExecution(new ExecutionData(0x1234, "foo/MyClass",
				new boolean[] { false, true, true }));
		out.close();
		return f;
	}

}
