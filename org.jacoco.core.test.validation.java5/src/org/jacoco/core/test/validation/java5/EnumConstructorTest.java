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
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.EnumConstructorTarget;
import org.junit.Test;

/**
 * Test of filtering of enum constructors.
 */
public class EnumConstructorTest extends ValidationTestBase {

	public EnumConstructorTest() {
		super(EnumConstructorTarget.class);
	}

	/**
	 * {@link EnumConstructorTarget.ImplicitConstructor}
	 */
	@Test
	public void implicit_constructor_should_be_filtered() {
		// without filter next line is partly covered:
		assertLine("implicitConstructor", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link EnumConstructorTarget.ExplicitNonEmptyConstructor#ExplicitNonEmptyConstructor()}
	 */
	@Test
	public void explicit_non_empty_constructor_should_not_be_filtered() {
		assertLine("explicitNonEmptyConstructor", ICounter.NOT_COVERED);
	}

	/**
	 * {@link EnumConstructorTarget.ExplicitEmptyConstructor#ExplicitEmptyConstructor()}
	 */
	@Test
	public void explicit_empty_constructor_should_be_filtered() {
		// without filter next line is not covered:
		assertLine("explicitEmptyConstructor", ICounter.EMPTY);
	}

	/**
	 * {@link EnumConstructorTarget.ExplicitEmptyConstructor#ExplicitEmptyConstructor(Object)}
	 */
	@Test
	public void explicit_empty_constructor_with_parameters_should_not_be_filtered() {
		assertLine("explicitEmptyConstructorWithParameter",
				ICounter.NOT_COVERED);
	}

}
