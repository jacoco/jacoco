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
		assertSnapshot(ControlStructuresTarget.class, "missedIfBlock",
				"if.txt");
		assertSnapshot(ControlStructuresTarget.class, "missedWhileBlock",
				"while.txt");
		assertSnapshot(ControlStructuresTarget.class, "executedDoWhileBlock",
				"do_while.txt");
		assertSnapshot(ControlStructuresTarget.class, "missedForBlock",
				"for.txt");
		assertSnapshot(ControlStructuresTarget.class, "missedForEachBlock",
				"for_each.txt");
		// tableSwitchWithHit
		// continuedTableSwitchWithHit
		// tableSwitchWithoutHit
		// lookupSwitchWithHit
		// continuedLookupSwitchWithHit
		// lookupSwitchWithoutHit
		// switchImplicitDefaultNotExecuted
		// breakStatement
		// continueStatement
		// conditionalReturn
		// implicitReturn
		// explicitReturn
	}

}
