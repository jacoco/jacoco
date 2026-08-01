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
package org.jacoco.core.test.validation.java27.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * Test target for <a href="https://openjdk.org/jeps/532">JEP 532</a> (preview).
 */
public class PrimitivePatternsTarget {

	record R(String c1, int c2) {
	}

	private static void nestedInRecordInInstanceof(Object o) {
		if (o instanceof R(String _, byte _)) { // assertFullyCovered(1,3)
			nop(); // assertFullyCovered()
		} // assertEmpty()
	}

	private static String nestedInRecordInSwitch(Object o) {
		return switch (o) { // assertFullyCovered(0,3)
		case R(String _, byte _) -> // assertFullyCovered(0,2)
			"R(String,byte)"; // assertFullyCovered()
		case R(String _, int _) -> // assertPartlyCovered(1,1)
			"R(Double,int)"; // assertFullyCovered()
		default -> // assertEmpty()
			"default"; // assertFullyCovered()
		};
	}

	public static void main(String[] args) {
		nestedInRecordInInstanceof(new R("", 1));
		nestedInRecordInInstanceof(new Object());

		nop(nestedInRecordInSwitch(new R("", 1)));
		nop(nestedInRecordInSwitch(new R("", 128)));
		nop(nestedInRecordInSwitch(new Object()));
	}

}
