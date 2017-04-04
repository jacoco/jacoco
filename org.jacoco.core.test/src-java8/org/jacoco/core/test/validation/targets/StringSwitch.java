/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target is a switch statement with a String (Java 7).
 */
public class StringSwitch {

	private static void uncovered(String s) {
		nop(); // $line-beforeSwitch$
		switch (s) { // $line-switch$
		case "a":
			nop(); // $line-caseA$
		case "b":
			nop(); // $line-caseB$
		case "\0a":
			nop(); // $line-case0A$
		default:
			nop(); // $line-default$
		}
		if ("c".equals(s)) // $line-afterSwitch$
			nop();
	}

	private static void covered(String s) {
		switch (s) { // $line-coveredSwitch$
		case "a":
			nop();
		case "b":
			nop();
		case "\0a":
			nop();
		default:
			nop();
		}
	}

	private static void switchByStringHashCode(String s) {
		switch (s.hashCode()) { // $line-switchByStringHashCode$
		case 97:
			nop();
		case 98:
			nop();
		}
	}

	public static void main(String[] args) {
		uncovered("c");

		covered("\0a");
		covered("a");
		covered("b");
		covered("c");
	}

}
