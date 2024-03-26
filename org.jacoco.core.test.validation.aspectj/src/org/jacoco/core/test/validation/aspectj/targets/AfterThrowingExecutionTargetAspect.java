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
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AfterThrowingExecutionTargetAspect {

	@AfterThrowing("execution(* org.jacoco.core.test.validation.aspectj.targets.AfterThrowingExecutionTarget.foo1())")
	public void after_joinpoint(JoinPoint joinPoint) {
		System.out.println("after " + joinPoint);
	}

	@AfterThrowing("execution(* org.jacoco.core.test.validation.aspectj.targets.AfterThrowingExecutionTarget.foo2())")
	public void after_empty() {
		System.out.println("after ");
	}

	@AfterThrowing(value = "execution(* org.jacoco.core.test.validation.aspectj.targets.AfterThrowingExecutionTarget.foo3())", throwing = "e")
	public void after_joinpoint_value(JoinPoint joinPoint, Exception e) {
		System.out.println("after " + joinPoint);
	}

	@AfterThrowing(value = "execution(* org.jacoco.core.test.validation.aspectj.targets.AfterThrowingExecutionTarget.foo4())", throwing = "e")
	public void after_value(Exception e) {
		System.out.println("after");
	}
}
