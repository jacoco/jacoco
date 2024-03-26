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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExecutionTargetAspect {

	@Before("execution(* org.jacoco.core.test.validation.aspectj.targets.BeforeExecutionTarget.foo1())")
	public void before_joinpoint(JoinPoint joinPoint) {
		System.out.println("before " + joinPoint);
	}

	@Before("execution(* org.jacoco.core.test.validation.aspectj.targets.BeforeExecutionTarget.foo2())")
	public void before_empty() {
		System.out.println("before");
	}
}
