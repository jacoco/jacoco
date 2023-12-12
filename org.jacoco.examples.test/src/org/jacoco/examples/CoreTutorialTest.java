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

import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for {@link CoreTutorial}.
 */
public class CoreTutorialTest {

	@Rule
	public ConsoleOutput console = new ConsoleOutput();

	@Test
	public void testRunExample() throws Exception {
		new CoreTutorial(console.stream).execute();

		console.expect(containsLine("0 of 3 methods missed"));
		console.expect(containsLine("1 of 5 complexity missed"));
		console.expect(containsLine("Line 47: "));
		console.expect(containsLine("Line 48: green"));
		console.expect(containsLine("Line 49: yellow"));
		console.expect(containsLine("Line 50: red"));
	}
}
