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
 * Unit test for {@link KotlinCoroutineFilter}.
 */
public class KotlinCoroutineFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinCoroutineFilter();

	@Test
	public void should_filter_suspending_lambdas_generated_by_Kotlin_1_3_30() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"invokeSuspend", "(Ljava/lang/Object;)Ljava/lang/Object;", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/intrinsics/IntrinsicsKt",
				"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
		final Range range1 = new Range();
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 4);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		// line of "runBlocking"
		m.visitFieldInsn(Opcodes.GETFIELD, "Target", "label", "I");
		final Label dflt = new Label();
		final Label state0 = new Label();
		final Label state1 = new Label();
		m.visitTableSwitchInsn(0, 1, dflt, state0, state1);

		m.visitLabel(state0);

		{
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/ResultKt",
					"throwOnFailure", "(Ljava/lang/Object;)V", false);
			range1.toInclusive = m.instructions.getLast();
		}

		// line before "suspendingFunction"
		m.visitInsn(Opcodes.NOP);

		// line of "suspendingFunction"
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "", "suspendingFunction",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", false);

		m.visitInsn(Opcodes.DUP);
		final Range range2 = new Range();
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 4);
		final Label continuationLabelAfterLoadedResult = new Label();
		m.visitJumpInsn(Opcodes.IF_ACMPNE, continuationLabelAfterLoadedResult);
		// line of "runBlocking"
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(state1);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Target", "I$0", "I");
		m.visitVarInsn(Opcodes.ISTORE, 3);

		{
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/ResultKt",
					"throwOnFailure", "(Ljava/lang/Object;)V", false);
		}
		m.visitVarInsn(Opcodes.ALOAD, 1);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(continuationLabelAfterLoadedResult);

		// line after "suspendingFunction"
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(dflt);
		final Range range0 = new Range();
		range0.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("call to 'resume' before 'invoke' with coroutine");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IllegalStateException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range0.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(range0, range1, range2);
	}

	/**
	 * <pre>
	 *     runBlocking {
	 *         val x = 42
	 *         nop(x)
	 *         suspendingFunction()
	 *         nop(x)
	 *     }
	 * </pre>
	 */
	@Test
	public void should_filter_suspending_lambdas() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"invokeSuspend", "(Ljava/lang/Object;)Ljava/lang/Object;", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/intrinsics/IntrinsicsKt",
				"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
		final Range range1 = new Range();
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 4);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		// line of "runBlocking"
		m.visitFieldInsn(Opcodes.GETFIELD, "Target", "label", "I");
		final Label dflt = new Label();
		final Label state0 = new Label();
		final Label state1 = new Label();
		m.visitTableSwitchInsn(0, 1, dflt, state0, state1);

		m.visitLabel(state0);

		{
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.DUP);
			m.visitTypeInsn(Opcodes.INSTANCEOF, "kotlin/Result$Failure");
			Label label = new Label();
			m.visitJumpInsn(Opcodes.IFEQ, label);
			m.visitTypeInsn(Opcodes.CHECKCAST, "kotlin/Result$Failure");
			m.visitFieldInsn(Opcodes.GETFIELD, "kotlin/Result$Failure",
					"exception", "Ljava/lang/Throwable");
			m.visitInsn(Opcodes.ATHROW);
			m.visitInsn(Opcodes.POP);
			range1.toInclusive = m.instructions.getLast();
			m.visitLabel(label);
		}

		// line before "suspendingFunction"
		m.visitInsn(Opcodes.NOP);

		// line of "suspendingFunction"
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "", "suspendingFunction",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", false);

		m.visitInsn(Opcodes.DUP);
		final Range range2 = new Range();
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 4);
		final Label continuationLabelAfterLoadedResult = new Label();
		m.visitJumpInsn(Opcodes.IF_ACMPNE, continuationLabelAfterLoadedResult);
		// line of "runBlocking"
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(state1);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Target", "I$0", "I");
		m.visitVarInsn(Opcodes.ISTORE, 3);

		{
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.DUP);
			m.visitTypeInsn(Opcodes.INSTANCEOF, "kotlin/Result$Failure");
			final Label label = new Label();
			m.visitJumpInsn(Opcodes.IFEQ, label);
			m.visitTypeInsn(Opcodes.CHECKCAST, "kotlin/Result$Failure");
			m.visitFieldInsn(Opcodes.GETFIELD, "kotlin/Result$Failure",
					"exception", "Ljava/lang/Throwable");
			m.visitInsn(Opcodes.ATHROW);
			m.visitInsn(Opcodes.POP);
			m.visitLabel(label);
		}
		m.visitVarInsn(Opcodes.ALOAD, 1);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(continuationLabelAfterLoadedResult);

		// line after "suspendingFunction"
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(dflt);
		final Range range0 = new Range();
		range0.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("call to 'resume' before 'invoke' with coroutine");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IllegalStateException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range0.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(range0, range1, range2);
	}

	/**
	 * <pre>
	 *     suspend fun example() {
	 *         suspendingFunction()
	 *         nop()
	 *     }
	 * </pre>
	 */
	@Test
	public void should_filter_suspending_functions() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_STATIC, "example",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		final int continuationArgumentIndex = 0;
		final int continuationIndex = 2;

		m.visitVarInsn(Opcodes.ALOAD, continuationArgumentIndex);
		final Range range1 = new Range();
		range1.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.INSTANCEOF, "ExampleKt$example$1");
		final Label createStateInstance = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, createStateInstance);

		m.visitVarInsn(Opcodes.ALOAD, continuationArgumentIndex);
		m.visitTypeInsn(Opcodes.CHECKCAST, "ExampleKt$example$1");
		m.visitVarInsn(Opcodes.ASTORE, continuationIndex);

		m.visitVarInsn(Opcodes.ALOAD, continuationIndex);
		m.visitFieldInsn(Opcodes.GETFIELD, "ExampleKt$example$1", "label", "I");

		m.visitLdcInsn(Integer.valueOf(Integer.MIN_VALUE));
		m.visitInsn(Opcodes.IAND);
		m.visitJumpInsn(Opcodes.IFEQ, createStateInstance);

		m.visitVarInsn(Opcodes.ALOAD, continuationIndex);
		m.visitInsn(Opcodes.DUP);
		m.visitFieldInsn(Opcodes.GETFIELD, "ExampleKt$example$1", "label", "I");

		m.visitLdcInsn(Integer.valueOf(Integer.MIN_VALUE));
		m.visitInsn(Opcodes.ISUB);
		m.visitFieldInsn(Opcodes.PUTFIELD, "ExampleKt$example$1", "label", "I");

		final Label afterCoroutineStateCreated = new Label();
		m.visitJumpInsn(Opcodes.GOTO, afterCoroutineStateCreated);

		m.visitLabel(createStateInstance);

		m.visitTypeInsn(Opcodes.NEW, "ExampleKt$example$1");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, continuationArgumentIndex);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "ExampleKt$example$1",
				"<init>", "(Lkotlin/coroutines/Continuation;)V", false);

		m.visitVarInsn(Opcodes.ASTORE, continuationIndex);

		m.visitLabel(afterCoroutineStateCreated);

		m.visitVarInsn(Opcodes.ALOAD, continuationIndex);
		m.visitFieldInsn(Opcodes.GETFIELD, "ExampleKt$example$1", "result",
				"Ljava/lang/Object;");
		m.visitVarInsn(Opcodes.ASTORE, 1);

		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/intrinsics/IntrinsicsKt",
				"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);

		// line of "fun"
		m.visitVarInsn(Opcodes.ASTORE, 3);

		m.visitVarInsn(Opcodes.ALOAD, continuationIndex);
		m.visitFieldInsn(Opcodes.GETFIELD, "ExampleKt$example$1", "label", "I");
		final Label dflt = new Label();
		final Label state0 = new Label();
		final Label state1 = new Label();
		m.visitTableSwitchInsn(0, 1, dflt, state0, state1);

		m.visitLabel(state0);

		{
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.DUP);
			m.visitTypeInsn(Opcodes.INSTANCEOF, "kotlin/Result$Failure");
			Label label = new Label();
			m.visitJumpInsn(Opcodes.IFEQ, label);
			m.visitTypeInsn(Opcodes.CHECKCAST, "kotlin/Result$Failure");
			m.visitFieldInsn(Opcodes.GETFIELD, "kotlin/Result$Failure",
					"exception", "Ljava/lang/Throwable");
			m.visitInsn(Opcodes.ATHROW);
			m.visitInsn(Opcodes.POP);
			range1.toInclusive = m.instructions.getLast();
			m.visitLabel(label);
		}

		// line of "suspendingFunction"
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "", "suspendingFunction",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", false);

		m.visitInsn(Opcodes.DUP);
		final Range range2 = new Range();
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 3);
		final Label continuationLabelAfterLoadedResult = new Label();
		m.visitJumpInsn(Opcodes.IF_ACMPNE, continuationLabelAfterLoadedResult);
		// line of "fun"
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(state1);

		{
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.DUP);
			m.visitTypeInsn(Opcodes.INSTANCEOF, "kotlin/Result$Failure");
			final Label label = new Label();
			m.visitJumpInsn(Opcodes.IFEQ, label);
			m.visitTypeInsn(Opcodes.CHECKCAST, "kotlin/Result$Failure");
			m.visitFieldInsn(Opcodes.GETFIELD, "kotlin/Result$Failure",
					"exception", "Ljava/lang/Throwable");
			m.visitInsn(Opcodes.ATHROW);
			m.visitInsn(Opcodes.POP);
			m.visitLabel(label);
		}
		m.visitVarInsn(Opcodes.ALOAD, 1);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(continuationLabelAfterLoadedResult);

		// line after "suspendingFunction"
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(dflt);
		final Range range0 = new Range();
		range0.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("call to 'resume' before 'invoke' with coroutine");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IllegalStateException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range0.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(range0, range1, range2);
	}

	/**
	 * <pre>
	 *     suspend fun example(b: Boolean) {
	 *         if (b)
	 *             suspendingFunction()
	 *         else
	 *             suspendingFunction()
	 *     }
	 * </pre>
	 */
	@Test
	public void should_filter_suspending_functions_with_tail_call_optimization() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example",
				"(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		final Label exit = new Label();

		m.visitVarInsn(Opcodes.ILOAD, 1);
		final Label next = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, next);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "", "suspendingFunction",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", false);
		final Range range1 = new Range();
		{
			m.visitInsn(Opcodes.DUP);
			range1.fromInclusive = m.instructions.getLast();
			m.visitMethodInsn(Opcodes.INVOKESTATIC,
					"kotlin/coroutines/intrinsics/IntrinsicsKt",
					"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
			final Label label = new Label();
			m.visitJumpInsn(Opcodes.IF_ACMPNE, label);
			m.visitInsn(Opcodes.ARETURN);
			m.visitLabel(label);
			m.visitInsn(Opcodes.POP);
			range1.toInclusive = m.instructions.getLast();
		}

		m.visitJumpInsn(Opcodes.GOTO, exit);
		m.visitLabel(next);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "", "suspendingFunction",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", false);
		final Range range2 = new Range();
		{
			m.visitInsn(Opcodes.DUP);
			range2.fromInclusive = m.instructions.getLast();
			m.visitMethodInsn(Opcodes.INVOKESTATIC,
					"kotlin/coroutines/intrinsics/IntrinsicsKt",
					"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
			final Label label = new Label();
			m.visitJumpInsn(Opcodes.IF_ACMPNE, label);
			m.visitInsn(Opcodes.ARETURN);
			m.visitLabel(label);
			m.visitInsn(Opcodes.POP);
			range2.toInclusive = m.instructions.getLast();
		}

		m.visitLabel(exit);
		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
	}

}
