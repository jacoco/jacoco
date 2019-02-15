/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertEquals;

public class CondyProbeArrayStrategyTest {

	private final CondyProbeArrayStrategy strategy = new CondyProbeArrayStrategy(
			"ClassName", 1L, new OfflineInstrumentationAccessGenerator());

	@Test
	public void should_not_add_fields() {
		final ClassNode c = new ClassNode();
		strategy.addMembers(c, 1);

		assertEquals(0, c.fields.size());
	}

	@Test
	public void should_add_bootstrap_method() {
		final ClassNode c = new ClassNode();
		strategy.addMembers(c, 1);

		assertEquals(1, c.methods.size());

		final MethodNode m = c.methods.get(0);
		assertEquals(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE
				| Opcodes.ACC_STATIC, m.access);
		assertEquals("$jacocoInit", m.name);
		assertEquals(
				"(Ljava/lang/invoke/MethodHandle$Lookup;Ljava/lang/String;Ljava/lang/Class;)[Z",
				m.desc);

		assertEquals(4, m.maxStack);
		assertEquals(3, m.maxLocals);
	}

}
