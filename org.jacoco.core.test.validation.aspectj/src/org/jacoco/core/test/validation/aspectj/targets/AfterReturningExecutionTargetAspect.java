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
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AfterReturningExecutionTargetAspect {

	@AfterReturning("execution(* org.jacoco.core.test.validation.aspectj.targets.AfterReturningExecutionTarget.foo1())")
	public void after_joinpoint(JoinPoint joinPoint) {
		System.out.println("after " + joinPoint);
	}

	@AfterReturning("execution(* org.jacoco.core.test.validation.aspectj.targets.AfterReturningExecutionTarget.foo2())")
	public void after_empty() {
		System.out.println("after");
	}

	@AfterReturning(value = "execution(* org.jacoco.core.test.validation.aspectj.targets.AfterReturningExecutionTarget.foo3())", returning = "val")
	public void after_joinpoint_value(JoinPoint joinPoint, Object val) {
		System.out.println("after " + joinPoint);
	}

	@AfterReturning(value = "execution(* org.jacoco.core.test.validation.aspectj.targets.AfterReturningExecutionTarget.foo4())", returning = "val")
	public void after_value(Object val) {
		System.out.println("after");
	}
}
