/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java8;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java8.targets.AnnotationOnLocalVariableTarget;

/**
 * Test of ASM bug
 * <a href="https://gitlab.ow2.org/asm/asm/issues/317815">#317815</a>
 */
public class AnnotationOnLocalVariableTest extends ValidationTestBase {

	public AnnotationOnLocalVariableTest() {
		super(AnnotationOnLocalVariableTarget.class);
	}

}
