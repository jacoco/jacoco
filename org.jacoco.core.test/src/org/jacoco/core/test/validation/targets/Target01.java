/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.i2;
import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.jacoco.core.test.validation.targets.Stubs.t;

import java.util.Collections;

/**
 * This target exercises a set of common Java control structures.
 */
public class Target01 {

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

		nop(); // $line-unconditional$

	}

	private static void missedIfBlock() {

		if (f()) { // $line-iffalse$
			nop(); // $line-missedif$
		} else {
			nop(); // $line-executedelse$
		}

	}

	private static void executedIfBlock() {

		if (t()) { // $line-iftrue$
			nop(); // $line-executedif$
		} else {
			nop(); // $line-missedelse$
		}

	}

	private static void missedWhileBlock() {

		while (f()) { // $line-whilefalse$
			nop(); // $line-missedwhile$
		}

	}

	private static void alwaysExecutedWhileBlock() {

		while (t()) { // $line-whiletrue$
			if (t()) {
				break;
			}
		}

	}

	private static void executedWhileBlock() {

		int i = 0;
		while (i++ < 3) { // $line-whiletruefalse$
			nop(); // $line-executedwhile$
		}

	}

	private static void executedDoWhileBlock() {

		do {
			nop(); // $line-executeddowhile$
		} while (f()); // $line-executeddowhilefalse$

	}

	private static void missedForBlock() {

		for (nop(); f(); nop()) { // $line-missedforincrementer$
			nop(); // $line-missedfor$
		}

	}

	private static void executedForBlock() {

		for (int j = 0; j < 1; j++) { // $line-executedforincrementer$
			nop(); // $line-executedfor$
		}

	}

	private static void missedForEachBlock() {

		for (Object o : Collections.emptyList()) { // $line-missedforeachincrementer$
			nop(o); // $line-missedforeach$
		}

	}

	private static void executedForEachBlock() {

		for (Object o : Collections.singleton(new Object())) { // $line-executedforeachincrementer$
			nop(o); // $line-executedforeach$
		}

	}

	private static void tableSwitchWithHit() {

		switch (i2()) { // $line-tswitch1$
		case 1:
			nop(); // $line-tswitch1case1$
			break;
		case 2:
			nop(); // $line-tswitch1case2$
			break;
		case 3:
			nop(); // $line-tswitch1case3$
			break;
		default:
			nop(); // $line-tswitch1default$
			break;
		}

	}

	private static void continuedTableSwitchWithHit() {

		switch (i2()) { // $line-tswitch2$
		case 1:
			nop(); // $line-tswitch2case1$
		case 2:
			nop(); // $line-tswitch2case2$
		case 3:
			nop(); // $line-tswitch2case3$
		default:
			nop(); // $line-tswitch2default$
		}

	}

	private static void tableSwitchWithoutHit() {

		switch (i2()) { // $line-tswitch3$
		case 3:
			nop(); // $line-tswitch3case1$
			break;
		case 4:
			nop(); // $line-tswitch3case2$
			break;
		case 5:
			nop(); // $line-tswitch3case3$
			break;
		default:
			nop(); // $line-tswitch3default$
			break;
		}

	}

	private static void lookupSwitchWithHit() {

		switch (i2()) { // $line-lswitch1$
		case -123:
			nop(); // $line-lswitch1case1$
			break;
		case 2:
			nop(); // $line-lswitch1case2$
			break;
		case 456:
			nop(); // $line-lswitch1case3$
			break;
		default:
			nop(); // $line-lswitch1default$
			break;
		}

	}

	private static void continuedLookupSwitchWithHit() {

		switch (i2()) { // $line-lswitch2$
		case -123:
			nop(); // $line-lswitch2case1$
		case 2:
			nop(); // $line-lswitch2case2$
		case 456:
			nop(); // $line-lswitch2case3$
		default:
			nop(); // $line-lswitch2default$
		}

	}

	private static void lookupSwitchWithoutHit() {

		switch (i2()) { // $line-lswitch3$
		case -123:
			nop(); // $line-lswitch3case1$
			break;
		case 456:
			nop(); // $line-lswitch3case2$
			break;
		case 789:
			nop(); // $line-lswitch3case3$
			break;
		default:
			nop(); // $line-lswitch3default$
			break;
		}

	}

	private static void breakStatement() {

		while (true) {
			if (t()) {
				break; // $line-executedbreak$
			}
			nop(); // $line-missedafterbreak$
		}

	}

	private static void continueStatement() {

		for (int j = 0; j < 1; j++) {
			if (t()) {
				continue; // $line-executedcontinue$
			}
			nop(); // $line-missedaftercontinue$
		}

	}

	private static void conditionalReturn() {

		if (t()) {
			return; // $line-conditionalreturn$
		}
		nop(); // $line-afterconditionalreturn$

	}

	private static void implicitReturn() {

	} // $line-implicitreturn$

	private static void explicitReturn() {

		return; // $line-explicitreturn$

	} // $line-afterexplicitreturn$

}
