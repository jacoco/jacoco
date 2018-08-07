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
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinWhenExpressionTarget;
import org.junit.Test;

/**
 * Test of <code>when</code> expressions.
 */
public class KotlinWhenExpressionTest extends ValidationTestBase {

	public KotlinWhenExpressionTest() {
		super(KotlinWhenExpressionTarget.class);
	}

	/**
	 * {@link KotlinWhenExpressionTarget#whenSealed(KotlinWhenExpressionTarget.Sealed)}
	 */
	@Test
	public void whenSealed() {
		assertLine("whenSealed.when", ICounter.FULLY_COVERED);
		assertLine("whenSealed.case1", ICounter.FULLY_COVERED, 0, 2);
		// without filter next line covered partly and has one uncovered branch:
		assertLine("whenSealed.case2", ICounter.FULLY_COVERED);
		assertLine("whenSealed.return", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link KotlinWhenExpressionTarget#whenSealedRedundantElse(KotlinWhenExpressionTarget.Sealed)}
	 */
	@Test
	public void whenSealedRedundantElse() {
		assertLine("whenSealedRedundantElse.when", ICounter.FULLY_COVERED);
		assertLine("whenSealedRedundantElse.case1", ICounter.FULLY_COVERED, 0, 2);
		assertLine("whenSealedRedundantElse.case2", ICounter.FULLY_COVERED, 1, 1);
		assertLine("whenSealedRedundantElse.else", ICounter.NOT_COVERED);
		assertLine("whenSealedRedundantElse.return", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link KotlinWhenExpressionTarget#whenEnum(KotlinWhenExpressionTarget.Enum)}
	 */
	@Test
	public void whenEnum() {
		assertLine("whenEnum.when", ICounter.FULLY_COVERED, 1, 2);
		assertLine("whenEnum.case1", ICounter.FULLY_COVERED);
		assertLine("whenEnum.case2", ICounter.PARTLY_COVERED);
		assertLine("whenEnum.return", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link KotlinWhenExpressionTarget#whenEnumRedundantElse(KotlinWhenExpressionTarget.Enum)}
	 */
	@Test
	public void whenEnumRedundantElse() {
		assertLine("whenEnumRedundantElse.when", ICounter.FULLY_COVERED, 1, 2);
		assertLine("whenEnumRedundantElse.case1", ICounter.FULLY_COVERED);
		assertLine("whenEnumRedundantElse.case2", ICounter.FULLY_COVERED);
		assertLine("whenEnumRedundantElse.else", ICounter.NOT_COVERED);
		assertLine("whenEnumRedundantElse.return", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link KotlinWhenExpressionTarget#whenString(String)}
	 */
	@Test
	public void whenString() {
		assertLine("whenString.when", ICounter.FULLY_COVERED, 2, 7);
		assertLine("whenString.case1", ICounter.FULLY_COVERED);
		assertLine("whenString.case2", ICounter.FULLY_COVERED);
		assertLine("whenString.case2", ICounter.FULLY_COVERED);
		assertLine("whenString.else", ICounter.FULLY_COVERED);
		assertLine("whenString.return", ICounter.FULLY_COVERED);
	}

}
