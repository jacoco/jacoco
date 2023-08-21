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
package org.jacoco.core.test.validation.aspectj;

import org.jacoco.core.test.validation.Source;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.aspectj.targets.AroundCallTarget;
import org.jacoco.core.test.validation.aspectj.targets.AroundExecutionTarget;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AroundCallTargetTest extends ValidationTestBase {
	public AroundCallTargetTest() {
		super(AroundCallTarget.class);
	}

	@Test
	public void assert_no_missed_lines() {
		assertEquals(0,
				getSource().getCoverage().getLineCounter().getMissedCount());
	}

	@Test
	public void assert_no_missed_instructions() {

		for (Source.Line line : getSource().getLines()) {
			if (line.getCoverage().getInstructionCounter()
					.getMissedCount() != 0) {
				System.out.println("missed instructions on line " + line.getNr()
						+ " " + line.getText());
			}
		}

		assertEquals(0, getSource().getCoverage().getInstructionCounter()
				.getMissedCount());
	}

}
