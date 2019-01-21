/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java8.targets;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This test target contains annotation on local variable.
 */
public class AnnotationOnLocalVariableTarget {

	@Documented
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.TYPE_USE)
	@interface NonNull {
	}

	private static Object legacy() {
		return new Object();
	}

	public static void main(String[] args) {
		@NonNull
		Object o = legacy(); // assertFullyCovered()
	}

}
