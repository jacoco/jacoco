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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class AspectTarget {

	public static void main(String[] args) {
		System.out.println("fooBarBaz");
	}

	@Before("execution(* fooBarBaz.*())")
	public void beforeAspect(JoinPoint joinPoint) {
		System.out.println("before " + joinPoint);
	}

	@After("execution(* fooBarBaz.*())")
	public void afterAspect(JoinPoint joinPoint) {
		System.out.println("after " + joinPoint);
	}

	@Around("execution(* fooBarBaz.*())")
	public Object aroundAspect(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.println("before " + joinPoint);
		return joinPoint.proceed();
	}
}
