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
		switch (e) { // $line-switch$
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
