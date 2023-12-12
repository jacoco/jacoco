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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link TryWithResourcesJavac11Filter}.
 */
public class TryWithResourcesJavac11FilterTest extends FilterTestBase {

	private final TryWithResourcesJavac11Filter filter = new TryWithResourcesJavac11Filter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	/**
	 * <pre>
	 *   try (r = new ...) {
	 *     ...
	 *   }
	 * </pre>
	 */
	@Test
	public void without_null_check() {
		final Range range1 = new Range();
		final Range range2 = new Range();

		final Label e = new Label();
		final Label t = new Label();

		final Label handler = new Label();
		m.visitTryCatchBlock(handler, handler, handler, "java/lang/Throwable");

		m.visitInsn(Opcodes.NOP);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		range1.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Resource", "close", "()V",
				false);
		m.visitJumpInsn(Opcodes.GOTO, e);
		range1.toInclusive = m.instructions.getLast();

		m.visitLabel(handler);
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Resource", "close", "()V",
				false);
		m.visitJumpInsn(Opcodes.GOTO, t);

		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"addSuppressed", "(Ljava/lang/Throwable;)V", false);
		m.visitLabel(t);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();

		m.visitLabel(e);

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
	}

	/**
	 * <pre>
	 *   try (r = open()) {
	 *     ...
	 *   }
	 * </pre>
	 */
	@Test
	public void with_null_check() {
		final Range range1 = new Range();
		final Range range2 = new Range();

		final Label e = new Label();
		final Label t = new Label();

		final Label handler = new Label();
		m.visitTryCatchBlock(handler, handler, handler, "java/lang/Throwable");

		m.visitInsn(Opcodes.NOP);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		range1.fromInclusive = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFNULL, e);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Resource", "close", "()V",
				false);
		m.visitJumpInsn(Opcodes.GOTO, e);
		range1.toInclusive = m.instructions.getLast();

		m.visitLabel(handler);
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitJumpInsn(Opcodes.IFNULL, t);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Resource", "close", "()V",
				false);
		m.visitJumpInsn(Opcodes.GOTO, t);

		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"addSuppressed", "(Ljava/lang/Throwable;)V", false);
		m.visitLabel(t);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();

		m.visitLabel(e);

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
	}

}
