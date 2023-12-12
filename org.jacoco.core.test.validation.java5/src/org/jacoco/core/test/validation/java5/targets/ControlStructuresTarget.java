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
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.i2;
import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.jacoco.core.test.validation.targets.Stubs.t;

import java.util.Collections;

/**
 * This target exercises a set of common Java control structures.
 */
public class ControlStructuresTarget {

	public static void main(String[] args) {

		unconditionalExecution();
		missedIfBlock();
		executedIfBlock();
		missedWhileBlock();
		alwaysExecutedWhileBlock();
		executedWhileBlock();
		executedDoWhileBlock();
		missedForBlock();
		executedForBlock();
		missedForEachBlock();
		executedForEachBlock();
		tableSwitchWithHit();
		continuedTableSwitchWithHit();
		tableSwitchWithoutHit();
		lookupSwitchWithHit();
		continuedLookupSwitchWithHit();
		lookupSwitchWithoutHit();
		breakStatement();
		continueStatement();
		conditionalReturn();
		implicitReturn();
		explicitReturn();

	}

	private static void unconditionalExecution() {

		nop(); // assertFullyCovered()

	}

	private static void missedIfBlock() {

		if (f()) { // assertFullyCovered(1, 1)
			nop(); // assertNotCovered()
		} else {
			nop(); // assertFullyCovered()
		}

	}

	private static void executedIfBlock() {

		if (t()) { // assertFullyCovered(1, 1)
			nop(); // assertFullyCovered()
		} else {
			nop(); // assertNotCovered()
		}

	}

	private static void missedWhileBlock() {

		while (f()) { // assertFullyCovered(1, 1)
			nop(); // assertNotCovered()
		}

	}

	private static void alwaysExecutedWhileBlock() {

		while (t()) { // assertFullyCovered(1, 1)
			if (t()) {
				break;
			}
		}

	}

	private static void executedWhileBlock() {

		int i = 0;
		while (i++ < 3) { // assertFullyCovered(0, 2)
			nop(); // assertFullyCovered()
		}

	}

	private static void executedDoWhileBlock() {

		do {
			nop(); // assertFullyCovered()
		} while (f()); // assertFullyCovered(1, 1)

	}

	private static void missedForBlock() {

		for (nop(); f(); nop()) { // assertPartlyCovered(1, 1)
			nop(); // assertNotCovered()
		}

	}

	private static void executedForBlock() {

		for (int j = 0; j < 1; j++) { // assertFullyCovered(0, 2)
			nop(); // assertFullyCovered()
		}

	}

	private static void missedForEachBlock() {

		for (Object o : Collections.emptyList()) { // assertPartlyCovered(1, 1)
			nop(o); // assertNotCovered()
		}

	}

	private static void executedForEachBlock() {

		for (Object o : Collections.singleton(new Object())) { // assertFullyCovered(0,2)
			nop(o); // assertFullyCovered()
		}

	}

	private static void tableSwitchWithHit() {

		switch (i2()) { // assertFullyCovered(3, 1)
		case 1:
			nop(); // assertNotCovered()
			break;
		case 2:
			nop(); // assertFullyCovered()
			break;
		case 3:
			nop(); // assertNotCovered()
			break;
		default:
			nop(); // assertNotCovered()
			break;
		}

	}

	private static void continuedTableSwitchWithHit() {

		switch (i2()) { // assertFullyCovered(3, 1)
		case 1:
			nop(); // assertNotCovered()
		case 2:
			nop(); // assertFullyCovered()
		case 3:
			nop(); // assertFullyCovered()
		default:
			nop(); // assertFullyCovered()
		}

	}

	private static void tableSwitchWithoutHit() {

		switch (i2()) { // assertFullyCovered(3, 1)
		case 3:
			nop(); // assertNotCovered()
			break;
		case 4:
			nop(); // assertNotCovered()
			break;
		case 5:
			nop(); // assertNotCovered()
			break;
		default:
			nop(); // assertFullyCovered()
			break;
		}

	}

	private static void lookupSwitchWithHit() {

		switch (i2()) { // assertFullyCovered(3, 1)
		case -123:
			nop(); // assertNotCovered()
			break;
		case 2:
			nop(); // assertFullyCovered()
			break;
		case 456:
			nop(); // assertNotCovered()
			break;
		default:
			nop(); // assertNotCovered()
			break;
		}

	}

	private static void continuedLookupSwitchWithHit() {

		switch (i2()) { // assertFullyCovered(3, 1)
		case -123:
			nop(); // assertNotCovered()
		case 2:
			nop(); // assertFullyCovered()
		case 456:
			nop(); // assertFullyCovered()
		default:
			nop(); // assertFullyCovered()
		}

	}

	private static void lookupSwitchWithoutHit() {

		switch (i2()) { // assertFullyCovered(3, 1)
		case -123:
			nop(); // assertNotCovered()
			break;
		case 456:
			nop(); // assertNotCovered()
			break;
		case 789:
			nop(); // assertNotCovered()
			break;
		default:
			nop(); // assertFullyCovered()
			break;
		}

	}

	private static void breakStatement() {

		while (true) {
			if (t()) {
				break; // assertFullyCovered()
			}
			nop(); // assertNotCovered()
		}

	}

	private static void continueStatement() {

		for (int j = 0; j < 1; j++) {
			if (t()) {
				continue; // assertFullyCovered()
			}
			nop(); // assertNotCovered()
		}

	}

	private static void conditionalReturn() {

		if (t()) {
			return; // assertFullyCovered()
		}
		nop(); // assertNotCovered()

	}

	private static void implicitReturn() {

	} // assertFullyCovered()

	private static void explicitReturn() {

		return; // assertFullyCovered()

	} // assertEmpty()

}
