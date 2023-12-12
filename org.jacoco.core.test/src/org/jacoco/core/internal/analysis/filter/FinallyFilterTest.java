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
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link FinallyFilter}.
 */
public class FinallyFilterTest implements IFilterOutput {

	private final IFilter filter = new FinallyFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	/**
	 * <pre>
	 *   try {
	 *     ...
	 *     if (...) {
	 *       ...
	 *       return;
	 *     } else {
	 *       ...
	 *       return;
	 *     }
	 *   } finally {
	 *     ...
	 *   }
	 * </pre>
	 */
	@Test
	public void should_analyze_control_flow() {
		final Label start1 = new Label();
		final Label end1 = new Label();
		final Label start2 = new Label();
		final Label end2 = new Label();
		final Label finallyStart = new Label();

		m.visitTryCatchBlock(start1, end1, finallyStart, null);
		m.visitTryCatchBlock(start2, end2, finallyStart, null);

		m.visitLabel(start1);
		// jump to another region associated with same handler:
		m.visitJumpInsn(Opcodes.IFEQ, start2);
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(end1);

		m.visitInsn(Opcodes.NOP); // finally block
		shouldMergeLast();
		m.visitInsn(Opcodes.RETURN);

		m.visitLabel(start2);
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(end2);
		m.visitInsn(Opcodes.NOP); // finally block
		shouldMergeLast();
		m.visitInsn(Opcodes.RETURN);

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.NOP); // finally block
		shouldMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.ATHROW);
		shouldIgnoreLast();

		execute();
	}

	// === try/catch/finally ===

	@Test
	public void javac_try_catch_finally() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label catchStart = new Label();
		final Label catchEnd = new Label();
		final Label finallyStart = new Label();
		final Label finallyEnd = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(catchStart, catchEnd, finallyStart, null);
		m.visitTryCatchBlock(tryStart, tryEnd, finallyStart, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);
		shouldIgnoreLast();

		m.visitLabel(catchStart);
		m.visitInsn(Opcodes.NOP); // catch body
		m.visitLabel(catchEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitInsn(Opcodes.ATHROW);

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.ATHROW);
		shouldIgnoreLast();
		m.visitLabel(finallyEnd);

		m.visitInsn(Opcodes.NOP);

		execute();
	}

	@Test
	public void ecj_try_catch_finally() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label catchStart = new Label();
		final Label catchEnd = new Label();
		final Label finallyStart = new Label();
		final Label finallyEnd = new Label();
		final Label after = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(tryStart, catchEnd, finallyStart, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);

		m.visitLabel(catchStart);
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.NOP); // catch body
		m.visitLabel(catchEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitJumpInsn(Opcodes.GOTO, after);
		shouldIgnoreLast();

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.ATHROW);
		shouldIgnoreLast();
		m.visitLabel(finallyEnd);

		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitLabel(after);
		m.visitInsn(Opcodes.NOP);

		execute();
	}

	// === empty catch ===

	/**
	 * javac 1.5 - 1.7
	 */
	@Test
	public void javac_empty_catch() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label catchStart = new Label();
		final Label catchEnd = new Label();
		final Label finallyStart = new Label();
		final Label finallyEnd = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(tryStart, tryEnd, finallyStart, null);
		m.visitTryCatchBlock(catchStart, catchEnd, finallyStart, null);
		// actually one more useless TryCatchBlock for ASTORE in finally

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);
		shouldIgnoreLast();

		m.visitLabel(catchStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitLabel(catchEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);
		shouldIgnoreLast();

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.ATHROW);
		shouldIgnoreLast();
		m.visitLabel(finallyEnd);

		m.visitInsn(Opcodes.NOP);

		execute();
	}

	/**
	 * javac >= 1.8
	 *
	 * Probably related to https://bugs.openjdk.java.net/browse/JDK-7093325
	 */
	@Test
	public void javac_8_empty_catch() throws Exception {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label catchStart = new Label();
		final Label finallyStart = new Label();
		final Label finallyEnd = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(tryStart, tryEnd, finallyStart, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);
		shouldIgnoreLast();
		shouldIgnoreLast();

		m.visitLabel(catchStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);
		shouldIgnoreLast();

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.ATHROW);
		shouldIgnoreLast();
		m.visitLabel(finallyEnd);

		execute();
	}

	@Test
	public void ecj_empty_catch() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label catchStart = new Label();
		final Label catchEnd = new Label();
		final Label finallyStart = new Label();
		final Label finallyEnd = new Label();
		final Label after = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(tryStart, catchEnd, finallyStart, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);

		m.visitLabel(catchStart);
		m.visitInsn(Opcodes.POP);
		m.visitLabel(catchEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitJumpInsn(Opcodes.GOTO, after);
		shouldIgnoreLast();

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		shouldIgnoreLast();
		m.visitInsn(Opcodes.ATHROW);
		shouldIgnoreLast();
		m.visitLabel(finallyEnd);

		m.visitInsn(Opcodes.NOP); // finally body
		shouldMergeLast();
		m.visitLabel(after);
		m.visitInsn(Opcodes.NOP);

		execute();
	}

	// === always completes abruptly ===

	@Test
	public void javac_always_completes_abruptly() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label finallyStart = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, finallyStart, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitInsn(Opcodes.RETURN); // finally body

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.RETURN); // finally body

		execute();
	}

	@Test
	public void ecj_always_completes_abruptly() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label finallyStart = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, tryEnd, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitJumpInsn(Opcodes.GOTO, finallyStart);
		m.visitLabel(tryEnd);

		m.visitInsn(Opcodes.POP);
		m.visitLabel(finallyStart);
		m.visitInsn(Opcodes.RETURN); // finally body

		execute();
	}

	private final Set<AbstractInsnNode> expectedIgnored = new HashSet<AbstractInsnNode>();
	private final Set<AbstractInsnNode> actualIgnored = new HashSet<AbstractInsnNode>();
	private final Set<AbstractInsnNode> expectedMerged = new HashSet<AbstractInsnNode>();
	private final Set<AbstractInsnNode> actualMerged = new HashSet<AbstractInsnNode>();

	private void shouldMergeLast() {
		expectedMerged.add(m.instructions.getLast());
	}

	private void shouldIgnoreLast() {
		expectedIgnored.add(m.instructions.getLast());
	}

	private void execute() {
		filter.filter(m, new FilterContextMock(), this);
		assertEquals("ignored", toIndexes(expectedIgnored),
				toIndexes(actualIgnored));
		assertEquals("merged", toIndexes(expectedMerged),
				toIndexes(actualMerged));
	}

	@SuppressWarnings("boxing")
	private Set<Integer> toIndexes(Set<AbstractInsnNode> set) {
		final Set<Integer> result = new HashSet<Integer>();
		for (final AbstractInsnNode i : set) {
			result.add(m.instructions.indexOf(i));
		}
		return result;
	}

	public void ignore(final AbstractInsnNode fromInclusive,
			final AbstractInsnNode toInclusive) {
		for (AbstractInsnNode i = fromInclusive; i != toInclusive; i = i
				.getNext()) {
			actualIgnored.add(i);
		}
		actualIgnored.add(toInclusive);
	}

	public void merge(final AbstractInsnNode i1, final AbstractInsnNode i2) {
		if (actualMerged.isEmpty() || actualMerged.contains(i1)
				|| actualMerged.contains(i2)) {
			actualMerged.add(i1);
			actualMerged.add(i2);
		} else {
			fail();
		}
	}

	public void replaceBranches(final AbstractInsnNode source,
			final Set<AbstractInsnNode> newTargets) {
		fail();
	}

}
