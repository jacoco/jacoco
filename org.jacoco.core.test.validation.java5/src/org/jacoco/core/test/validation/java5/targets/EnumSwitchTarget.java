/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
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
 * This test target is a switch statement with a enum.
 */
public class EnumSwitchTarget {

	private enum E {
		V1, V2
	}

	private static void example(E e) {
		switch (e) { // assertSwitch()
		case V1:
			nop("V1");
			break;
		case V2:
		default:
			nop("V2");
			break;
		}
	}

	public static void main(String[] args) {
		example(E.V1);
		example(E.V2);
	}

}
