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
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * Test target containing chained and nested method invocations.
 */
public class MethodInvocationsTarget {

	public static void main(String[] args) {
		chained();
		nested();
	}

	private static void chained() {
		new MethodInvocationsTarget() // assertFullyCovered()
				.chained("first") // assertMethodInvocation()
				.chained("second"); // assertMethodInvocation()
	}

	private static void nested() {
		nested( // assertFullyCovered()
				nested("")); // assertMethodInvocation()

		/* For comparison with above: */
		nested( // assertFullyCovered()
				""); // assertNonMethodInvocation()
	}

	private MethodInvocationsTarget chained(final String s) {
		nop(s);
		return this;
	}

	private static Object nested(final Object o) {
		return o;
	}

}
