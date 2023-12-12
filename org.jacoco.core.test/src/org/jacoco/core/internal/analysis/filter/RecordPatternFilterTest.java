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
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link RecordPatternFilter}.
 */
public class RecordPatternFilterTest extends FilterTestBase {

	private final IFilter filter = new RecordPatternFilter();

	/**
	 * <pre>
	 *   record Point(int x, int y) {}
	 *
	 *   void example(Object o) {
	 *     if (o instanceof Point(int x, int y)) {
	 *       ...
	 *     }
	 *   }
	 * </pre>
	 */
	@Test
	public void should_filter_instanceof() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);

		final Label start1 = new Label();
		final Label end1 = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start1, end1, handler, "java/lang/Throwable");
		final Label start2 = new Label();
		final Label end2 = new Label();
		m.visitTryCatchBlock(start2, end2, handler, "java/lang/Throwable");

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.INSTANCEOF, "Example$Point");
		final Label label1 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "Example$Point");
		m.visitVarInsn(Opcodes.ASTORE, 2);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLabel(start1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$Point", "x", "()I",
				false);
		m.visitLabel(end1);
		m.visitVarInsn(Opcodes.ISTORE, 5);
		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitVarInsn(Opcodes.ISTORE, 3);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLabel(start2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$Point", "y", "()I",
				false);
		m.visitLabel(end2);
		m.visitVarInsn(Opcodes.ISTORE, 5);
		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitVarInsn(Opcodes.ISTORE, 4);

		m.visitVarInsn(Opcodes.ILOAD, 3);
		m.visitVarInsn(Opcodes.ILOAD, 4);
		m.visitInsn(Opcodes.IADD);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example", "nop", "(I)V",
				false);

		m.visitLabel(label1);
		final Label label2 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label2);

		m.visitLabel(handler);
		final Range range = new Range();
		range.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/MatchException");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"toString", "()Ljava/lang/String;", false);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
				"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(label2);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(range, range);
	}

	/**
	 * <pre>
	 *   record Point(int x, int y) {}
	 *
	 *   void example(Object o) {
	 *     switch (o) {
	 *       case Point(int x, int y) -> ...
	 *       default -> ...
	 *     }
	 *   }
	 * </pre>
	 */
	@Test
	public void should_filter_switch() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);

		final Label start1 = new Label();
		final Label end1 = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start1, end1, handler, "java/lang/Throwable");
		final Label start2 = new Label();
		final Label end2 = new Label();
		m.visitTryCatchBlock(start2, end2, handler, "java/lang/Throwable");

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects",
				"requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;",
				false);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ILOAD, 3);
		m.visitInvokeDynamicInsn("typeSwitch", "(Ljava/lang/Object;I)I",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/SwitchBootstraps", "typeSwitch",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType("LExample$Point;") });
		final Label case1 = new Label();
		final Label dflt = new Label();
		m.visitLookupSwitchInsn(dflt, new int[] { 0 }, new Label[] { case1 });
		m.visitLabel(case1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitTypeInsn(Opcodes.CHECKCAST, "Example$Point");
		m.visitVarInsn(Opcodes.ASTORE, 4);

		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitLabel(start1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$Point", "x", "()I",
				false);
		m.visitLabel(end1);
		m.visitVarInsn(Opcodes.ISTORE, 7);
		m.visitVarInsn(Opcodes.ILOAD, 7);
		m.visitVarInsn(Opcodes.ISTORE, 5);

		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitLabel(start2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$Point", "y", "()I",
				false);
		m.visitLabel(end2);
		m.visitVarInsn(Opcodes.ISTORE, 7);
		m.visitVarInsn(Opcodes.ILOAD, 7);
		m.visitVarInsn(Opcodes.ISTORE, 6);

		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitVarInsn(Opcodes.ILOAD, 6);
		m.visitInsn(Opcodes.IADD);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example", "nop", "(I)V",
				false);
		final Label label1 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label1);

		m.visitLabel(dflt);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example", "nop", "(I)V",
				false);
		m.visitLabel(label1);
		final Label label2 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label2);

		m.visitLabel(handler);
		final Range range = new Range();
		range.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/MatchException");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"toString", "()Ljava/lang/String;", false);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
				"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(label2);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(range, range);
	}

}
