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
package org.jacoco.core.test.validation.java14.targets;

import static org.jacoco.core.test.validation.targets.Stubs.i1;
import static org.jacoco.core.test.validation.targets.Stubs.i2;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

import org.jacoco.core.test.validation.targets.Stubs;

/**
 * This target exercises switch expressions.
 */
public class SwitchExpressionsTarget {

	public static void main(String[] args) {

		switchExpressionWithArrows();
		multiValueSwitchExpressionWithArrows();
		switchExpressionWithArrowsAndYield();
		switchExpressionWithYield();

		exhaustiveSwitchExpression(Stubs.Enum.A);
		exhaustiveSwitchExpression(Stubs.Enum.B);
		exhaustiveSwitchExpression(Stubs.Enum.C);

	}

	private static void switchExpressionWithArrows() {

		nop(switch (i2()) { // assertFullyCovered(3, 1)
		case 1 -> i1(); // assertNotCovered()
		case 2 -> i1(); // assertFullyCovered()
		case 3 -> i1(); // assertNotCovered()
		default -> i1(); // assertNotCovered()
		});

	}

	private static void multiValueSwitchExpressionWithArrows() {

		nop(switch (i2()) { // assertFullyCovered(2, 1)
		case 1, 2 -> i1(); // assertFullyCovered()
		case 3, 4 -> i1(); // assertNotCovered()
		default -> i1(); // assertNotCovered()
		});

	}

	private static void switchExpressionWithArrowsAndYield() {

		nop(switch (i2()) { // assertFullyCovered(3, 1)
		case 1 -> {
			nop(); // assertNotCovered()
			yield i1(); // assertNotCovered()
		}
		case 2 -> {
			nop(); // assertFullyCovered()
			yield i1(); // assertFullyCovered()
		}
		case 3 -> {
			nop(); // assertNotCovered()
			yield i1(); // assertNotCovered()
		}
		default -> {
			nop(); // assertNotCovered()
			yield i1(); // assertNotCovered()
		}
		});

	}

	private static void switchExpressionWithYield() {

		nop(switch (i2()) { // assertFullyCovered(3, 1)
		case 1:
			nop(); // assertNotCovered()
			yield i1(); // assertNotCovered()
		case 2:
			nop(); // assertFullyCovered()
			yield i1(); // assertFullyCovered()
		case 3:
			nop(); // assertNotCovered()
			yield i1(); // assertNotCovered()
		default:
			nop(); // assertNotCovered()
			yield i1(); // assertNotCovered()
		});

	}

	private static void exhaustiveSwitchExpression(Stubs.Enum e) {

		nop(switch (e) { // assertExhaustiveSwitchExpression()
		case A -> i1(); // assertFullyCovered()
		case B -> i1(); // assertFullyCovered()
		case C -> i1(); // assertExhaustiveSwitchExpressionLastCase()
		}); // assertEmpty()

	}
}
