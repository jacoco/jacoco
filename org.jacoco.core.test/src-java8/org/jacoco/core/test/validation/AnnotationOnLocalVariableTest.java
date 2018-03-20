/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.targets.AnnotationOnLocalVariableTarget;
import org.junit.Test;

/**
 * Test of ASM bug
 * <a href="https://gitlab.ow2.org/asm/asm/issues/317815">#317815</a>
 */
public class AnnotationOnLocalVariableTest extends ValidationTestBase {

	public AnnotationOnLocalVariableTest() {
		super("src-java8", AnnotationOnLocalVariableTarget.class);
	}

	@Test
	public void testCoverageResult() {

		assertLine("var", ICounter.FULLY_COVERED);

	}

}
