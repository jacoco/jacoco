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

@SuppressWarnings("SpringAopPointcutExpressionInspection")
@Aspect
public class BeforeSetTargetAspect {

	@Before("set(* org.jacoco.core.test.validation.aspectj.targets.BeforeSetTarget.foo)")
	public void before_joinpoint(JoinPoint joinPoint) {
		System.out.println("before " + joinPoint);
	}

}
