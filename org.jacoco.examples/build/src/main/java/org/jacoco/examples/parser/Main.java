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
package org.jacoco.examples.parser;

import java.io.IOException;

public class Main {

	public static void main(final String[] args) throws IOException {
		for (String expression : args) {
			ExpressionParser parser = new ExpressionParser(expression);
			double result = parser.parse().evaluate();
			System.out.printf("%s = %s%n", expression, result);
		}
	}

}
