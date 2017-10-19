/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *******************************************************************************/
package org.jacoco.core.test.filter.targets;

public class Assert {

	public static boolean b = true;

	public static void simple() {
		assert b; // $line-simple$
	}

	public static void message() {
		assert b : "m"; // $line-message$
	}

	public static class SimpleClinit {
		static {
			assert b; // $line-clinit$
		}

		public static void init() {
		}
	}

	public static void main(String[] args) {
		simple();
		message();
		SimpleClinit.init();
	}

}
