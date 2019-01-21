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
