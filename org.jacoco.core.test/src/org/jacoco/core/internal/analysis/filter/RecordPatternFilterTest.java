/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
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

		assertIgnored(m, range, range);
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

		assertIgnored(m, range, range);
	}

	/**
	 * TODO link to JEP?
	 *
	 * <pre>
	 * record R(int c) {}
	 *
	 * String example(Object o) {
	 *     return switch (o) {
	 *         case R(int i) -> "case";
	 *         default -> "default";
	 *     }
	 * }
	 * </pre>
	 */
	@org.junit.Ignore
	@Test
	public void should_filter_primitive() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(Ljava/lang/Object;)Ljava/lang/String;", null,
				null);

		final Label labelPatternComponentStart = new Label();
		final Label labelPatternComponentEnd = new Label();
		final Label labelHandler = new Label();
		m.visitTryCatchBlock(labelPatternComponentStart,
				labelPatternComponentEnd, labelHandler, "java/lang/Throwable");

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects",
				"requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;",
				false);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		Label labelSwitch = new Label();
		m.visitLabel(labelSwitch);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitInvokeDynamicInsn("typeSwitch", "(Ljava/lang/Object;I)I",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/SwitchBootstraps", "typeSwitch",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType("LR;") });
		final Label labelPattern = new Label();
		final Label labelDefault = new Label();
		m.visitLookupSwitchInsn(labelDefault, new int[] { 0 },
				new Label[] { labelPattern });
		m.visitLabel(labelPattern);
		final Range components = new Range();
		components.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "R");
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitLabel(labelPatternComponentStart);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "R", "c", "()I", false);
		m.visitLabel(labelPatternComponentEnd);
		m.visitVarInsn(Opcodes.ISTORE, 5);
		// m.visitVarInsn(ILOAD, 5);
		// m.visitVarInsn(ISTORE, 6);
		m.visitInsn(Opcodes.ICONST_1);
		final Label labelRestart = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, labelRestart);
		// TODO including below GOTO?
		// m.visitVarInsn(ILOAD, 5);
		// m.visitVarInsn(ISTORE, 4);
		final Label labelCase = new Label();
		m.visitJumpInsn(Opcodes.GOTO, labelCase);
		components.toInclusive = m.instructions.getLast();

		m.visitLabel(labelRestart);
		final Range restart = new Range();
		restart.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ICONST_1);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, labelSwitch);
		restart.toInclusive = m.instructions.getLast();

		m.visitLabel(labelCase);
		m.visitLdcInsn("case");
		// TODO rename
		Label label10 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label10);

		m.visitLabel(labelDefault);
		m.visitLdcInsn("default");
		m.visitJumpInsn(Opcodes.GOTO, label10);

		m.visitLabel(label10);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(labelHandler);
		final Range handler = new Range();
		handler.fromInclusive = m.instructions.getLast();
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
		handler.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);
		assertIgnored(m, handler, restart, components);
	}

	/**
	 * <pre>
	 * record R(Object c) {}
	 *
	 * String example(Object o) {
	 *    return switch (o) {
	 *    case R(Float _), R(Double _) -> "case";
	 *    default -> "default";
	 *    }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_multiple_patterns_in_case() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(Ljava/lang/Object;)Ljava/lang/String;", null,
				null);

		final Label labelPattern1ComponentStart = new Label();
		final Label labelPattern1ComponentEnd = new Label();
		final Label labelHandler = new Label();
		m.visitTryCatchBlock(labelPattern1ComponentStart,
				labelPattern1ComponentEnd, labelHandler, "java/lang/Throwable");
		final Label labelPattern2ComponentStart = new Label();
		final Label labelPattern2ComponentEnd = new Label();
		m.visitTryCatchBlock(labelPattern2ComponentStart,
				labelPattern2ComponentEnd, labelHandler, "java/lang/Throwable");

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects",
				"requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;",
				false);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		final Label labelSwitch = new Label();
		m.visitLabel(labelSwitch);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitInvokeDynamicInsn("typeSwitch", "(Ljava/lang/Object;I)I",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/SwitchBootstraps", "typeSwitch",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType("LR;"), Type.getType("LR;") });
		final Label labelPattern1 = new Label();
		final Label labelDefault = new Label();
		m.visitLookupSwitchInsn(labelDefault, new int[] { 0, 1 },
				new Label[] { labelPattern1, labelPattern1 });

		m.visitLabel(labelPattern1);
		final Range components = new Range();
		components.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "R");
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitLabel(labelPattern1ComponentStart);
		final Range component1 = new Range();
		component1.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "R", "c",
				"()Ljava/lang/Object;", false);
		m.visitLabel(labelPattern1ComponentEnd);
		m.visitVarInsn(Opcodes.ASTORE, 5);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/Float");
		final Label labelPattern2 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, labelPattern2);
		final Range p = new Range(m.instructions.getLast(),
				m.instructions.getLast());
		final Label labelCase = new Label();
		m.visitJumpInsn(Opcodes.GOTO, labelCase);
		component1.toInclusive = m.instructions.getLast();

		m.visitLabel(labelPattern2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "R");
		m.visitVarInsn(Opcodes.ASTORE, 4);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitLabel(labelPattern2ComponentStart);
		final Range component2 = new Range();
		component2.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "R", "c",
				"()Ljava/lang/Object;", false);
		m.visitLabel(labelPattern2ComponentEnd);
		m.visitVarInsn(Opcodes.ASTORE, 5);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/Double");
		final Label labelRestart = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, labelRestart);
		m.visitJumpInsn(Opcodes.GOTO, labelCase);
		component2.toInclusive = m.instructions.getLast();
		components.toInclusive = m.instructions.getLast();

		m.visitLabel(labelRestart);
		final Range restart = new Range();
		restart.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ICONST_2);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, labelSwitch);
		restart.toInclusive = m.instructions.getLast();

		m.visitLabel(labelCase);
		m.visitLdcInsn("case");
		// TODO rename
		final Label label12 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label12);

		m.visitLabel(labelDefault);
		m.visitLdcInsn("default");
		m.visitJumpInsn(Opcodes.GOTO, label12);

		m.visitLabel(label12);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(labelHandler);
		final Range handler = new Range();
		handler.fromInclusive = m.instructions.getLast();
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
		handler.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(m, handler, component1, handler, component2, restart,
				components);
	}

}
