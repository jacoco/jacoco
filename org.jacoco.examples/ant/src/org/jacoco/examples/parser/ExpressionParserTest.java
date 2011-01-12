/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.examples.parser;

import java.io.IOException;

public class ExpressionParserTest {

	private static void runTest(final String expression, final double expected)
			throws IOException {
		final ExpressionParser parser = new ExpressionParser(expression);
		final double actual = parser.parse().evaluate();
		System.out.println(expression + " evaluates to " + actual);
		if (actual != expected) {
			throw new AssertionError("But expected was " + expected);
		}
	}

	public static void main(final String[] args) throws IOException {
		runTest("2 * 3 + 4", 10);
		runTest("2 + 3 * 4", 14);
		runTest("(2 + 3) * 4", 20);
		runTest("2 * 2 * 2 * 2", 16);
		runTest("1 + 2 + 3 + 4", 10);
		runTest("2 * 3 + 2 * 5", 16);
	}

}
