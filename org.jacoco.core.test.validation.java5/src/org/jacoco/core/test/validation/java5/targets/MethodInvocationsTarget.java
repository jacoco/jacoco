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
		new MethodInvocationsTarget().chained();
		new MethodInvocationsTarget().nested();
	}

	private void chained() {
		m( // assertFullyCovered()
				new Object()) // assertEmptyWhenJavacOtherwiseFully()
				.m( // assertEmptyWhenJavacBelow8OtherwiseFully()
						new Object()); // assertEmptyWhenJavacOtherwiseFully()
	}

	private void nested() {
		m( // assertFullyCovered()
				m( // assertEmptyWhenJavacBelow8OtherwiseFully()
						new Object())); // assertEmptyWhenJavacOtherwiseFully()
	}

	protected MethodInvocationsTarget m(final Object o) {
		nop(o);
		return this;
	}

}
