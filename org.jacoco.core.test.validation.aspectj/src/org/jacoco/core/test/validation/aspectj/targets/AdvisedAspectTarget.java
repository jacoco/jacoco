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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AdvisedAspectTarget {

	public static void main(String[] args) {
		System.out.println("AdvisedAspectTarget"); // assertFullyCovered()
	}

	@Around("execution(* AdvisedAspectTarget.main(*))")
	public Object aroundMain(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		return proceedingJoinPoint.proceed(); // assertFullyCovered()
	}
}
