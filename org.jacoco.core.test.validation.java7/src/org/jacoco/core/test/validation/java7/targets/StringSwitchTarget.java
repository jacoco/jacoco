/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java7.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target is a switch statement with a String.
 */
public class StringSwitchTarget {

	private static void covered(Object s) {
		switch (String.valueOf(s)) { // assertSwitchCovered()
		case "a":
			nop("case a"); // assertFullyCovered()
			break;
		case "b":
			nop("case b"); // assertFullyCovered()
			break;
		case "\0a":
			nop("case \0a"); // assertFullyCovered()
			break;
		default:
			nop("case default"); // assertFullyCovered()
			break;
		}
	}

	private static void notCovered(Object s) {
		switch (String.valueOf(s)) { // assertSwitchNotCovered()
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

	private static void handwritten(String s) {
		int c = -1;
		switch (s.hashCode()) { // assertFullyCovered(2, 1)
		case 97:
			if ("a".equals(s)) { // assertFullyCovered(1, 1)
				c = 0;
			} else if ("\0a".equals(s)) {
				c = 1;
			}
			break;
		case 98:
			if ("b".equals(s)) {
				c = 2;
			}
			break;
		}
		switch (c) { // assertFullyCovered(3, 1)
		case 0:
			nop("case a"); // assertFullyCovered()
			break;
		case 1:
			nop("case \0a"); // assertNotCovered()
			break;
		case 2:
			nop("case b");
			break;
		default:
			nop("default");
			break;
		}
	}

	/**
	 * In this case javac generates <code>LOOKUPSWITCH</code> for second switch.
	 */
	private static void lookupswitch(Object s) {
		switch (String.valueOf(s)) { // assertLookupswitch()
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

	public static void main(String[] args) {
		covered("");
		covered("a");
		covered("b");
		covered("\0a");

		handwritten("a");
	}

}
