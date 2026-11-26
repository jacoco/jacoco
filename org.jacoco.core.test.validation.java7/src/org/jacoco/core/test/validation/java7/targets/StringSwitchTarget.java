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
package org.jacoco.core.test.validation.java7.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

import org.jacoco.core.test.validation.targets.Stubs;

/**
 * This test target is a switch statement with a String.
 */
public class StringSwitchTarget {

	private static void covered(Object s) {
		switch (String.valueOf(s)) { // assertFullyCovered(0, 5)
		case "a":
			nop("case a"); // assertFullyCovered()
			break;
		case "b":
			nop("case b"); // assertFullyCovered()
			break;
		case "c":
			nop("case c"); // assertFullyCovered()
			break;
		case "\0a":
			nop("case \0a"); // assertFullyCovered()
			break;
		default:
			nop("default"); // assertFullyCovered()
			break;
		}
	}

	private static void executedWithSameHashCodeAsFirstCase() {
		switch (Stubs.string("\0a")) { // assertFullyCovered(2, 1)
		case "a":
			nop("case a"); // assertNotCovered()
			break;
		case "b":
			nop("case b"); // assertNotCovered()
			break;
		default:
			nop("default"); // assertFullyCovered()
			break;
		}
	}

	private static void notCovered(Object s) {
		switch (String.valueOf(s)) { // assertNotCovered(4, 0)
		case "a":
			nop("case a");
			break;
		case "b":
			nop("case b");
			break;
		case "\0a":
			nop("case \0a");
			break;
		default:
			nop("default");
			break;
		}
	}

	private static void implicitDefaultNotExecuted(Object s) {
		switch (String.valueOf(s)) { // assertFullyCovered(1, 3)
		case "a":
			nop("case a"); // assertFullyCovered()
			break;
		case "b":
			nop("case b"); // assertFullyCovered()
			break;
		case "c":
			nop("case c"); // assertFullyCovered()
			break;
		}
	}

	private static void handwritten(Object s) {
		String tmp = String.valueOf(s);
		int c = -1;
		switch (tmp.hashCode()) { // assertFullyCovered(3, 1)
		case 97:
			if (tmp.equals("\0a")) { // assertFullyCovered(1, 1)
				c = 2;
			} else if (tmp.equals("a")) {
				c = 0;
			}
			break;
		case 98:
			if (tmp.equals("b")) {
				c = 1;
			}
			break;
		case 99:
			if (tmp.equals("c")) {
				c = 3;
			}
			break;
		}
		switch (c) { // assertFullyCovered(4, 1)
		case 0:
			nop("case a"); // assertFullyCovered()
			break;
		case 1:
			nop("case b");
			break;
		case 2:
			nop("case c");
			break;
		case 3:
			nop("case \0a"); // assertNotCovered()
			break;
		default:
			nop("default");
			break;
		}
	}

	/**
	 * In this case javac generates <code>LOOKUPSWITCH</code>es.
	 */
	private static void fewCases(Object s) {
		switch (String.valueOf(s)) { // assertNotCovered(3, 0)
		case "a":
			nop("case a");
			break;
		case "b":
			nop("case b");
			break;
		default:
			nop("default");
			break;
		}
	}

	private static void default_is_first(Object s) {
		switch (String.valueOf(s)) { // assertFullyCovered(0, 3)
		default:
			nop("default");
			break;
		case "a":
			nop("case a");
			break;
		case "b":
			nop("case b");
			break;
		}
	}

	public static void main(String[] args) {
		covered("");
		covered("a");
		covered("b");
		covered("c");
		covered("\0a");

		executedWithSameHashCodeAsFirstCase();

		implicitDefaultNotExecuted("a");
		implicitDefaultNotExecuted("b");
		implicitDefaultNotExecuted("c");

		handwritten("a");

		default_is_first("");
		default_is_first("a");
		default_is_first("b");
	}

}
