/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link FilterTestBase}.
 */
public class FilterTestBaseTest extends FilterTestBase {

	@Test
	public void assertIgnored_should_throw_ComparisonFailure() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);
		final Range range = new Range();
		m.visitInsn(Opcodes.NOP);
		range.fromInclusive = m.instructions.getFirst();
		range.toInclusive = m.instructions.getLast();

		try {
			assertIgnored(m, range);
			fail("exception expected");
		} catch (final ComparisonFailure e) {
			assertEquals("", e.getActual());
			assertEquals("range 0 from instruction 0 to 0\n", e.getExpected());
			assertTrue(e.getMessage().startsWith("ignored ranges expected:"));
		}
	}

}
