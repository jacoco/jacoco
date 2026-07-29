/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test;

/**
 * Poor man's replacement for <a href="https://openjdk.org/jeps/378">Java 15
 * Text Blocks</a>.
 */
public final class TextBlock {

	private TextBlock() {
	}

	/**
	 * As with Text Blocks, the LF character is used as the line terminator in
	 * the resulting string.
	 */
	public static String lines(String... lines) {
		final StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line).append('\n');
		}
		return sb.toString();
	}

}
