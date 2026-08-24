/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.ControlStructuresTarget;
import org.junit.Test;

/**
 * Tests of basic Java control structures.
 */
public class ControlStructuresTest extends ValidationTestBase {

	public ControlStructuresTest() {
		super(ControlStructuresTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		final String prefix = isJDKCompiler ? "" : "ecj/";
		assertSnapshot(ControlStructuresTarget.class, "missedIfBlock",
				prefix + "if.txt");
		assertSnapshot(ControlStructuresTarget.class, "missedWhileBlock",
				prefix + "while.txt");
		assertSnapshot(ControlStructuresTarget.class, "executedDoWhileBlock",
				"do_while.txt");
		assertSnapshot(ControlStructuresTarget.class, "missedForBlock",
				prefix + "for.txt");
		assertSnapshot(ControlStructuresTarget.class, "missedForEachBlock",
				prefix + "for_each.txt");
		assertSnapshot(ControlStructuresTarget.class, "tableSwitchWithHit",
				"tableswitch.txt");
		// continuedTableSwitchWithHit
		// tableSwitchWithoutHit
		// lookupSwitchWithHit
		assertSnapshot(ControlStructuresTarget.class, "lookupSwitchWithHit",
				"lookupswitch.txt");
		// continuedLookupSwitchWithHit
		// lookupSwitchWithoutHit
		// switchImplicitDefaultNotExecuted
		assertSnapshot(ControlStructuresTarget.class, "breakStatement",
				prefix + "break.txt");
		assertSnapshot(ControlStructuresTarget.class, "continueStatement",
				prefix + "continue.txt");
		// conditionalReturn
		// implicitReturn
		// explicitReturn
	}

}
