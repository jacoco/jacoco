/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class KotlinWhenSealedFilterTest implements IFilterOutput {

	private final KotlinWhenSealedFilter filter = new KotlinWhenSealedFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	@Test
	public void should_filter_implicit_else() {
		final Label label = new Label();

		final Range range1 = new Range();

		m.visitInsn(Opcodes.NOP);

		m.visitJumpInsn(Opcodes.IFEQ, label);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);

		final Range range2 = new Range();
		m.visitLabel(label);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, new FilterContextMock(), this);

		assertEquals(2, ignoredRanges.size());

		assertEquals(range1.fromInclusive, ignoredRanges.get(0).fromInclusive);
		assertEquals(range1.toInclusive, ignoredRanges.get(0).toInclusive);

		assertEquals(range2.fromInclusive, ignoredRanges.get(1).fromInclusive);
		assertEquals(range2.toInclusive, ignoredRanges.get(1).toInclusive);
	}

	@Test
	public void should_not_filter_explicit_else() {
		final Label label = new Label();

		m.visitInsn(Opcodes.NOP);

		m.visitJumpInsn(Opcodes.IFEQ, label);

		m.visitInsn(Opcodes.NOP);

		m.visitLabel(label);
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Throwable");
		m.visitInsn(Opcodes.ATHROW);

		filter.filter(m, new FilterContextMock(), this);

		assertEquals(0, ignoredRanges.size());
	}

	static class Range {
		AbstractInsnNode fromInclusive;
		AbstractInsnNode toInclusive;
	}

	private final List<Range> ignoredRanges = new ArrayList<Range>();

	public void ignore(final AbstractInsnNode fromInclusive,
			final AbstractInsnNode toInclusive) {
		final Range range = new Range();
		range.fromInclusive = fromInclusive;
		range.toInclusive = toInclusive;
		this.ignoredRanges.add(range);
	}

	public void merge(final AbstractInsnNode i1, final AbstractInsnNode i2) {
		fail();
	}

}
