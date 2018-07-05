/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class KotlinLateinitFilterTest implements IFilterOutput {
	private final KotlinLateinitFilter filter = new KotlinLateinitFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	@Test
	public void testLateinitBranchIsFiltered() {
		final Label l1 = new Label();
		final Label l2 = new Label();

		m.visitLabel(l1);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD,
				"com/better/alarm/background/VibrationService", "wakeLock",
				"Landroid/os/PowerManager$WakeLock;");
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, l2);

		final AbstractInsnNode expectedFrom = m.instructions.getLast();

		m.visitLdcInsn("wakelock");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"Ljava/lang/String;", false);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(l2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"android/os/PowerManager$WakeLock", "acquire", "", false);
		filter.filter(m, new FilterContextMock(), this);

		assertEquals(expectedFrom, fromInclusive);
		assertEquals(expectedTo, toInclusive);
	}

	public void ignore(AbstractInsnNode fromInclusive,
			AbstractInsnNode toInclusive) {
		assertNull(this.fromInclusive);
		this.fromInclusive = fromInclusive;
		this.toInclusive = toInclusive;
	}

	public void merge(final AbstractInsnNode i1, final AbstractInsnNode i2) {
		fail();
	}
}
