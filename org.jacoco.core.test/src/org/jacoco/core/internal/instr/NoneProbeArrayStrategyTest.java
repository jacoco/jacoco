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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;

/**
 * Unit tests for {@link NoneProbeArrayStrategy}.
 */
public class NoneProbeArrayStrategyTest {

	private NoneProbeArrayStrategy strategy;

	@Before
	public void setup() {
		strategy = new NoneProbeArrayStrategy();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void storeInstance_should_throw_UnsupportedOperationException() {
		strategy.storeInstance(null, false, 0);
	}

	@Test
	public void addMembers_should_not_add_members() {
		final ClassNode c = new ClassNode();
		strategy.addMembers(c, 0);

		assertEquals(0, c.methods.size());
		assertEquals(0, c.fields.size());
	}

}
