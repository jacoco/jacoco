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

public class AfterReturningExecutionTarget {

	public static void main(String[] args) {
		new AfterReturningExecutionTarget().foo1(); // assertFullyCovered()
		new AfterReturningExecutionTarget().foo2(); // assertFullyCovered()
		new AfterReturningExecutionTarget().foo3(); // assertFullyCovered()
		new AfterReturningExecutionTarget().foo4(); // assertFullyCovered()
	}

	public void foo1() {
		System.out.println("bar"); // assertFullyCovered()
	}
	public void foo2() {
		System.out.println("bar"); // assertFullyCovered()
	}
	public void foo3() {
		System.out.println("bar"); // assertFullyCovered()
	}
	public void foo4() {
		System.out.println("bar"); // assertFullyCovered()
	}
}
