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
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class PrivateEmptyNoArgConstructorFilterTest implements IFilterOutput {

	private final IFilter filter = new PrivateEmptyNoArgConstructorFilter();

	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	@Test
	public void test() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter("Foo", "java/lang/Object", m, this);

		assertEquals(m.instructions.getFirst(), fromInclusive);
		assertEquals(m.instructions.getLast(), toInclusive);
	}

	public void ignore(final AbstractInsnNode fromInclusive,
			final AbstractInsnNode toInclusive) {
		this.fromInclusive = fromInclusive;
		this.toInclusive = toInclusive;
	}

	public void merge(final AbstractInsnNode i1, final AbstractInsnNode i2) {
		fail();
	}

}
