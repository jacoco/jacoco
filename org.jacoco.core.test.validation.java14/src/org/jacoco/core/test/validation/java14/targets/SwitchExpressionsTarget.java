/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

/**
 * This target exercises switch expressions.
 */
public class SwitchExpressionsTarget {

	public static void main(String[] args) {

		switchExpressionsWithArrows();
		multiValueSwitchExpressionsWithArrows();
		switchExpressionsWithArrowsAndYield();
		switchExpressionsWithYield();

	}

	private static void switchExpressionsWithArrows() {

		nop(switch (i2()) { // assertFullyCovered(3, 1)
		case 1 -> i1(); // assertNotCovered()
		case 2 -> i1(); // assertFullyCovered()
		case 3 -> i1(); // assertNotCovered()
		default -> i1(); // assertNotCovered()
		});

	}

	private static void multiValueSwitchExpressionsWithArrows() {

		nop(switch (i2()) { // assertFullyCovered(2, 1)
		case 1, 2 -> i1(); // assertFullyCovered()
		case 3, 4 -> i1(); // assertNotCovered()
		default -> i1(); // assertNotCovered()
		});

	}

	private static void switchExpressionsWithArrowsAndYield() {

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

	private static void switchExpressionsWithYield() {

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
}
