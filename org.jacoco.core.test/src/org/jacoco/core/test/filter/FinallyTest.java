/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.filter;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.filter.targets.Finally;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.junit.Test;

/**
 * Test of filtering of duplicated bytecode of finally blocks.
 */
public class FinallyTest extends ValidationTestBase {

	public FinallyTest() {
		super(Finally.class);
	}

	/**
	 * {@link Finally#test()}
	 */
	@Test
	public void test() {
		assertLine("test.tryBlock", ICounter.FULLY_COVERED);
		assertLine("test.finallyBlock", ICounter.FULLY_COVERED);
		assertLine("test.after", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#catchNotExecuted()}
	 */
	@Test
	public void catchNotExecuted() {
		assertLine("catchNotExecuted.tryBlock", ICounter.FULLY_COVERED);
		assertLine("catchNotExecuted.catch",
				isJDKCompiler ? ICounter.NOT_COVERED : ICounter.PARTLY_COVERED);
		assertLine("catchNotExecuted.catchBlock", ICounter.NOT_COVERED);
		assertLine("catchNotExecuted.finallyBlock", ICounter.FULLY_COVERED);
		assertLine("catchNotExecuted.after", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#insideWhile()}
	 */
	@Test
	public void insideWhile() {
		assertLine("insideWhile.finally", ICounter.FULLY_COVERED);
		assertLine("insideWhile.finallyLastLine", isJDKCompiler
				? ICounter.PARTLY_COVERED : ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#insideDoWhile()}
	 */
	@Test
	public void insideDoWhile() {
		assertLine("insideDoWhile.finally", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#insideFor()}
	 */
	@Test
	public void insideFor() {
		assertLine("insideFor.finally", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#insideForEach()}
	 */
	@Test
	public void insideForEach() {
		assertLine("insideForEach.finally", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#branches(boolean)}
	 */
	@Test
	public void branches() {
		assertLine("conditionalJump", ICounter.FULLY_COVERED, 0, 2);
		assertLine("tableswitch", ICounter.FULLY_COVERED, 0, 2);
		assertLine("lookupswitch", ICounter.FULLY_COVERED, 0, 2);
	}

	/**
	 * {@link Finally#returnInBody()}
	 */
	@Test
	public void returnInBody() {
		assertLine("returnInBody", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#nested()}
	 */
	@Test
	public void nested() {
		assertLine("nested", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#noInstructionsAfterDuplicate()}
	 */
	@Test
	public void noInstructionsAfterDuplicate() {
		assertLine("noInstructionsAfterDuplicate.finally", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#alwaysCompletesAbruptly()}
	 */
	@Test
	public void alwaysCompletesAbruptly() {
		assertLine("alwaysCompletesAbruptly.tryBlock", ICounter.FULLY_COVERED);
		if (isJDKCompiler) {
			assertLine("alwaysCompletesAbruptly.finally", ICounter.EMPTY);
			assertLine("alwaysCompletesAbruptly.finallyBlock",
					ICounter.PARTLY_COVERED);
		} else {
			assertLine("alwaysCompletesAbruptly.finally",
					ICounter.PARTLY_COVERED);
			assertLine("alwaysCompletesAbruptly.finallyBlock",
					ICounter.FULLY_COVERED);
		}
	}

}
