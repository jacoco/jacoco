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
package org.jacoco.examples;

import static org.jacoco.examples.ConsoleOutput.containsLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for {@link ClassInfo}.
 */
public class ClassInfoTest {

	@Rule
	public ConsoleOutput console = new ConsoleOutput();

	@Test
	public void testRunExample() throws Exception {

		final String[] args = new String[] { createClassFile() };
		new ClassInfo(console.stream).execute(args);

		console.expect(containsLine(
				"class name:   org/jacoco/examples/ClassInfoTest"));
		console.expect(containsLine("methods:      3"));
		console.expect(containsLine("branches:     2"));
		console.expect(containsLine("complexity:   4"));
	}

	private String createClassFile() throws IOException {
		InputStream in = getClass()
				.getResource(getClass().getSimpleName() + ".class")
				.openStream();
		File f = File.createTempFile("Example", ".class");
		FileOutputStream out = new FileOutputStream(f);
		int b;
		while ((b = in.read()) != -1) {
			out.write(b);
		}
		in.close();
		out.close();
		return f.getPath();
	}
}
