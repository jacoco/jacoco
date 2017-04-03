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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class StringSwitchFilterTest {

	private final StringSwitchFilter filter = new StringSwitchFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	@Test
	public void ecj() {
		final FilterOutput expected = new FilterOutput();

		final Label h1 = new Label();
		final Label h2 = new Label();
		final Label dflt = new Label();
		final Label cases = new Label();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(0, 2, dflt, h1, h2);
		m.visitLabel(h1);
		final AbstractInsnNode fromInclusive = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFNE, cases);
		expected.ignoreJumpTarget(m.instructions.getLast());
		expected.remapJump(m.instructions.getLast(),
				fromInclusive.getPrevious());
		m.visitJumpInsn(Opcodes.IFNE, cases);
		expected.remapJump(m.instructions.getLast(),
				fromInclusive.getPrevious());
		m.visitJumpInsn(Opcodes.GOTO, dflt);
		m.visitLabel(h2);
		m.visitJumpInsn(Opcodes.IFNE, cases);
		expected.ignoreJumpTarget(m.instructions.getLast());
		expected.remapJump(m.instructions.getLast(),
				fromInclusive.getPrevious());
		m.visitJumpInsn(Opcodes.GOTO, dflt);
		final AbstractInsnNode toInclusive = m.instructions.getLast();
		m.visitLabel(cases);
		m.visitLabel(dflt);
		m.visitInsn(Opcodes.RETURN);

		expected.ignore(fromInclusive, toInclusive);

		final FilterOutput actual = new FilterOutput();
		filter.filter("Foo", "java/lang/Object", m, actual);

		assertEquals(expected, actual);
	}

	@Test
	public void javac() {
		final FilterOutput expected = new FilterOutput();

		final Label h1 = new Label();
		final Label h1_2 = new Label();
		final Label h2 = new Label();
		final Label after_h = new Label();
		final Label cases = new Label();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitLookupSwitchInsn(after_h, new int[] { 97, 98 },
				new Label[] { h1, h2 });
		final AbstractInsnNode fromInclusive = m.instructions.getLast();
		m.visitLabel(h1);
		m.visitJumpInsn(Opcodes.IFEQ, h1_2);
		expected.ignoreJumpTarget(m.instructions.getLast());
		m.visitLabel(h1_2);
		m.visitJumpInsn(Opcodes.IFEQ, after_h);
		expected.ignoreJumpTarget(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, after_h);
		m.visitLabel(h2);
		m.visitJumpInsn(Opcodes.IFEQ, after_h);
		m.visitJumpInsn(Opcodes.GOTO, after_h);
		final AbstractInsnNode toInclusive = m.instructions.getLast();
		m.visitLabel(after_h);
		m.visitTableSwitchInsn(0, 2, cases);
		m.visitLabel(cases);
		m.visitInsn(Opcodes.RETURN);

		expected.ignore(fromInclusive, toInclusive);

		final FilterOutput actual = new FilterOutput();
		filter.filter("Foo", "java/lang/Object", m, actual);

		assertEquals(expected, actual);
	}

	static class FilterOutput implements IFilterOutput {
		private final Set<AbstractInsnNode> ignored = new HashSet<AbstractInsnNode>();
		private final List<AbstractInsnNode> ignoredJumpTargets = new ArrayList<AbstractInsnNode>();
		private final Map<AbstractInsnNode, AbstractInsnNode> remappedJumps = new HashMap<AbstractInsnNode, AbstractInsnNode>();

		public void ignore(final AbstractInsnNode fromInclusive,
				final AbstractInsnNode toInclusive) {
			for (AbstractInsnNode i = fromInclusive; i != toInclusive; i = i
					.getNext()) {
				ignored.add(i);
			}
			ignored.add(toInclusive);
		}

		public void ignoreJumpTarget(final AbstractInsnNode instruction) {
			ignoredJumpTargets.add(instruction);
		}

		public void remapJump(final AbstractInsnNode original,
				final AbstractInsnNode remapped) {
			remappedJumps.put(original, remapped);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final FilterOutput that = (FilterOutput) o;
			return this.ignored.equals(that.ignored)
					&& this.ignoredJumpTargets.equals(that.ignoredJumpTargets)
					&& this.remappedJumps.equals(that.remappedJumps);
		}
	}

}
