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

import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class FinallyFilterTest {

	private final IFilter filter = new FinallyFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"m", "()V", null, null);

	/**
	 * javac for
	 *
	 * <pre>
	 *     try {
	 *         ...
	 *     } catch (RuntimeException e) {
	 *         ...
	 *     } catch (Exception e) {
	 *         ...
	 *         return ...;
	 *     } finally {
	 *         ...
	 *     }
	 * </pre>
	 */
	@Test
	public void javac() {
		final Label bodyStart = new Label();
		final Label bodyEnd = new Label();
		final Label catchStart = new Label();
		final Label catchEnd = new Label();
		final Label catch2Start = new Label();
		final Label catch2End = new Label();
		final Label finallyStart = new Label();
		m.visitTryCatchBlock(bodyStart, bodyEnd, catchStart,
				"java/lang/RuntimeException");
		m.visitTryCatchBlock(bodyStart, bodyEnd, catch2Start,
				"java/lang/Exception");
		m.visitTryCatchBlock(bodyStart, bodyEnd, finallyStart, null);
		m.visitTryCatchBlock(catchStart, catchEnd, finallyStart, null);
		m.visitTryCatchBlock(catch2Start, catch2End, finallyStart, null);

		m.visitLabel(bodyStart);
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(bodyEnd);
		m.visitInsn(Opcodes.NOP); // finally
		expectedMergeLast();
		final Label after = new Label();
		m.visitJumpInsn(Opcodes.GOTO, after);
		expectedIgnoreLast();

		m.visitLabel(catchStart);
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(catchEnd);
		m.visitInsn(Opcodes.NOP); // finally
		expectedMergeLast();
		m.visitJumpInsn(Opcodes.GOTO, after);
		expectedIgnoreLast();

		m.visitLabel(catch2Start);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitLabel(catch2End);
		m.visitInsn(Opcodes.NOP); // finally
		expectedMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		expectedIgnoreLast();
		m.visitInsn(Opcodes.NOP); // finally
		expectedMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 2);
		expectedIgnoreLast();
		m.visitInsn(Opcodes.ATHROW);
		expectedIgnoreLast();

		m.visitLabel(after);
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter("", "", m, output);

		assertEquals(expectedOutput.merged, output.merged);
		assertEquals(expectedOutput.ignored, output.ignored);
	}

	/**
	 * ECJ for
	 *
	 * <pre>
	 *     try {
	 *         ...
	 *     } catch (Exception e) {
	 *         ...
	 *     } finally {
	 *         ...
	 *     }
	 * </pre>
	 */
	@Test
	public void ecj() {
		final Label bodyStart = new Label();
		final Label bodyEnd = new Label();
		final Label catchStart = new Label();
		final Label catchEnd = new Label();
		final Label finallyHandler = new Label();
		final Label f = new Label();
		final Label after = new Label();
		m.visitTryCatchBlock(bodyStart, bodyEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(bodyStart, catchEnd, finallyHandler, null);

		m.visitLabel(bodyStart);
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(bodyEnd);
		m.visitJumpInsn(Opcodes.GOTO, f);

		m.visitLabel(catchStart);
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(catchEnd);
		m.visitInsn(Opcodes.NOP); // finally, can be omitted in case of throw
		expectedMergeLast();
		// following goto instruction has same line number as previous
		// instruction, so should be ignored, otherwise will result in partial
		// coverage of finally block if this catch block not executed:
		m.visitJumpInsn(Opcodes.GOTO, after); // can be return or throw
		expectedIgnoreLast();

		m.visitLabel(finallyHandler);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		expectedIgnoreLast();
		m.visitInsn(Opcodes.NOP); // finally
		expectedMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		expectedIgnoreLast();
		m.visitInsn(Opcodes.ATHROW);
		expectedIgnoreLast();

		m.visitLabel(f);
		m.visitInsn(Opcodes.NOP); // finally
		expectedMergeLast();

		m.visitLabel(after);
		m.visitInsn(Opcodes.RETURN);

		filter.filter("", "", m, output);

		assertEquals(expectedOutput.merged, output.merged);
		assertEquals(expectedOutput.ignored, output.ignored);
	}

	/**
	 * ECJ for
	 *
	 * <pre>
	 *     try {
	 *         ...
	 *     } finally {
	 *         ...
	 *     }
	 * </pre>
	 */
	@Test
	public void ecj2() {
		final Label bodyStart = new Label();
		final Label finallyHandler = new Label();
		final Label f = new Label();
		m.visitTryCatchBlock(bodyStart, finallyHandler, finallyHandler, null);

		m.visitLabel(bodyStart);
		m.visitInsn(Opcodes.NOP);
		m.visitJumpInsn(Opcodes.GOTO, f);

		m.visitLabel(finallyHandler);
		m.visitVarInsn(Opcodes.ASTORE, 0);
		expectedIgnoreLast();
		m.visitInsn(Opcodes.NOP); // finally
		expectedMergeLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		expectedIgnoreLast();
		m.visitInsn(Opcodes.ATHROW);
		expectedIgnoreLast();

		m.visitLabel(f);
		m.visitInsn(Opcodes.NOP); // finally
		expectedMergeLast();
		m.visitInsn(Opcodes.RETURN);

		filter.filter("", "", m, output);

		assertEquals(expectedOutput.merged, output.merged);
		assertEquals(expectedOutput.ignored, output.ignored);
	}

	/**
	 * @see #ecj_when_finally_block_always_completes_abruptly()
	 */
	@Test
	public void javac_when_finally_block_always_completes_abruptly() {
		final Label finallyHandler = new Label();
		final Label bodyStart = new Label();
		final Label bodyEnd = new Label();

		m.visitTryCatchBlock(bodyStart, bodyEnd, finallyHandler, null);

		m.visitLabel(bodyStart);
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(bodyEnd);
		m.visitInsn(Opcodes.NOP); // finally

		m.visitLabel(finallyHandler);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.NOP); // finally

		filter.filter("", "", m, output);

		assertEquals(expectedOutput.merged, output.merged);
		assertEquals(expectedOutput.ignored, output.ignored);
	}

	/**
	 * @see #javac_when_finally_block_always_completes_abruptly()
	 */
	@Test
	public void ecj_when_finally_block_always_completes_abruptly() {
		final Label bodyStart = new Label();
		final Label finallyHandler = new Label();
		final Label f = new Label();

		m.visitTryCatchBlock(bodyStart, finallyHandler, finallyHandler, null);

		m.visitLabel(bodyStart);
		m.visitInsn(Opcodes.NOP);
		m.visitJumpInsn(Opcodes.GOTO, f);

		m.visitLabel(finallyHandler);
		m.visitInsn(Opcodes.POP);
		m.visitLabel(f);
		m.visitInsn(Opcodes.NOP); // finally

		filter.filter("", "", m, output);

		assertEquals(expectedOutput.merged, output.merged);
		assertEquals(expectedOutput.ignored, output.ignored);
	}

	private void expectedIgnoreLast() {
		expectedOutput.ignored.add(m.instructions.getLast());
	}

	private void expectedMergeLast() {
		expectedOutput.merged.add(m.instructions.getLast());
	}

	private final Output expectedOutput = new Output();
	private final Output output = new Output();

	private static class Output implements IFilterOutput {
		private final Set<AbstractInsnNode> ignored = new HashSet<AbstractInsnNode>();
		private final Set<AbstractInsnNode> merged = new HashSet<AbstractInsnNode>();

		public void ignore(final AbstractInsnNode fromInclusive,
				final AbstractInsnNode toInclusive) {
			for (AbstractInsnNode i = fromInclusive; i != toInclusive; i = i
					.getNext()) {
				ignored.add(i);
			}
			ignored.add(toInclusive);
		}

		public void merge(final AbstractInsnNode i1,
				final AbstractInsnNode i2) {
			merged.add(i1);
			merged.add(i2);
		}
	}

}
