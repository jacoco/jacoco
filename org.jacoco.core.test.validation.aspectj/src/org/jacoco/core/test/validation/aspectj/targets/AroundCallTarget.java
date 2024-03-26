/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lars Grefer
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.aspectj.targets;

public class AroundCallTarget {

	public static void main(String[] args) {
		System.out.println("beforeCall");// assertFullyCovered()
		new AroundCallTarget().foo();// assertFullyCovered()
		System.out.println("afterCall");// assertFullyCovered()
	}

	public void foo() {
		System.out.println("AroundCallTarget.foo");// assertFullyCovered()
	}
}
