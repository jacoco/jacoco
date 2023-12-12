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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jacoco.cli.internal.CommandTestBase;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link Merge}.
 */
public class MergeTest extends CommandTestBase {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void should_print_usage_when_no_options_are_given()
			throws Exception {
		execute("merge");

		assertFailure();
		assertContains("\"--destfile\"", err);
		assertContains("java -jar jacococli.jar merge [<execfiles> ...]", err);
	}

	@Test
	public void should_print_warning_when_no_exec_files_are_provided()
			throws Exception {
		File dest = new File(tmp.getRoot(), "merged.exec");
		execute("merge", "--destfile", dest.getAbsolutePath());

		assertOk();
		assertContains("[WARN] No execution data files provided.", out);
		Set<String> names = loadExecFile(dest);
		assertEquals(Collections.emptySet(), names);
	}

	@Test
	public void should_merge_exec_files() throws Exception {
		File a = createExecFile("a");
		File b = createExecFile("b");
		File c = createExecFile("c");
		File dest = new File(tmp.getRoot(), "merged.exec");

		execute("merge", "--destfile", dest.getAbsolutePath(),
				a.getAbsolutePath(), b.getAbsolutePath(), c.getAbsolutePath());

		assertOk();
		Set<String> names = loadExecFile(dest);
		assertEquals(new HashSet<String>(Arrays.asList("a", "b", "c")), names);
	}

	private File createExecFile(String name) throws IOException {
		File file = new File(tmp.getRoot(), name + ".exec");
		final FileOutputStream execout = new FileOutputStream(file);
		ExecutionDataWriter writer = new ExecutionDataWriter(execout);
		writer.visitClassExecution(new ExecutionData(name.hashCode(), name,
				new boolean[] { true }));
		execout.close();
		return file;
	}

	private Set<String> loadExecFile(File file) throws IOException {
		ExecFileLoader loader = new ExecFileLoader();
		loader.load(file);
		Set<String> names = new HashSet<String>();
		for (ExecutionData d : loader.getExecutionDataStore().getContents()) {
			names.add(d.getName());
		}
		return names;
	}

}
