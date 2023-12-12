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
import static org.junit.Assert.assertTrue;

import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class CondyProbeArrayStrategyTest {

	private CondyProbeArrayStrategy strategy;

	@Before
	public void setup() {
		strategy = new CondyProbeArrayStrategy("ClassName", true, 1L,
				new OfflineInstrumentationAccessGenerator());
	}

	@Test
	public void should_store_instance_using_condy_and_checkcast() {
		final MethodNode m = new MethodNode();
		final int maxStack = strategy.storeInstance(m, false, 1);

		assertEquals(1, maxStack);

		final ConstantDynamic constantDynamic = (ConstantDynamic) ((LdcInsnNode) m.instructions
				.get(0)).cst;
		assertEquals("$jacocoData", constantDynamic.getName());
		assertEquals("Ljava/lang/Object;", constantDynamic.getDescriptor());

		final Handle bootstrapMethod = constantDynamic.getBootstrapMethod();
		assertEquals(Opcodes.H_INVOKESTATIC, bootstrapMethod.getTag());
		assertEquals("ClassName", bootstrapMethod.getOwner());
		assertEquals("$jacocoInit", bootstrapMethod.getName());
		assertEquals(
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)[Z",
				bootstrapMethod.getDesc());
		assertTrue(bootstrapMethod.isInterface());

		final TypeInsnNode castInstruction = (TypeInsnNode) m.instructions
				.get(1);
		assertEquals(Opcodes.CHECKCAST, castInstruction.getOpcode());
		assertEquals("[Z", castInstruction.desc);

		final VarInsnNode storeInstruction = (VarInsnNode) m.instructions
				.get(2);
		assertEquals(Opcodes.ASTORE, storeInstruction.getOpcode());
		assertEquals(1, storeInstruction.var);

		assertEquals(3, m.instructions.size());
	}

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
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)[Z",
				m.desc);

		assertEquals(4, m.maxStack);
		assertEquals(3, m.maxLocals);
	}

}
