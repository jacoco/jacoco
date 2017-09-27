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
package org.jacoco.core.test.filter.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target is a switch statement with a String.
 */
public class StringSwitch {

	private static void covered(Object s) {
		switch (String.valueOf(s)) { // $line-covered.switch$
		case "a":
			nop("case a"); // $line-covered.case1$
			break;
		case "b":
			nop("case b"); // $line-covered.case2$
			break;
		case "\0a":
			nop("case \0a"); // $line-covered.case3$
			break;
		default:
			nop("case default"); // $line-covered.default$
			break;
		}
	}

	private static void notCovered(Object s) {
		switch (String.valueOf(s)) { // $line-notCovered$
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
		switch (s.hashCode()) { // $line-handwritten.firstSwitch$
		case 97:
			if ("a".equals(s)) { // $line-handwritten.ignored$
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
		switch (c) { // $line-handwritten.secondSwitch$
		case 0:
			nop("case a"); // $line-handwritten.case1$
			break;
		case 1:
			nop("case \0a"); // $line-handwritten.case2$
			break;
		case 2:
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
