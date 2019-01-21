/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.examples;

import static org.jacoco.examples.ConsoleOutput.containsLine;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfo;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for {@link ExecDump}.
 */
public class ExecDumpTest {

	@Rule
	public ConsoleOutput console = new ConsoleOutput();

	@Test
	public void testRunExample() throws Exception {

		final String file = createExecFile();
		final String[] args = new String[] { file };
		new ExecDump(console.stream).execute(args);

		console.expect(containsLine("exec file: " + file));
		console.expect(containsLine("CLASS ID         HITS/PROBES   CLASS NAME"));
		console.expect(containsString("Session \"testid\":"));
		console.expect(containsLine("0000000000001234    2 of   3   foo/MyClass"));
	}

	private String createExecFile() throws IOException {
		File f = File.createTempFile("jacoco", ".exec");
		final FileOutputStream out = new FileOutputStream(f);
		final ExecutionDataWriter writer = new ExecutionDataWriter(out);
		writer.visitSessionInfo(new SessionInfo("testid", 1, 2));
		writer.visitClassExecution(new ExecutionData(0x1234, "foo/MyClass",
				new boolean[] { false, true, true }));
		writer.flush();
		out.close();
		return f.getPath();
	}
}
