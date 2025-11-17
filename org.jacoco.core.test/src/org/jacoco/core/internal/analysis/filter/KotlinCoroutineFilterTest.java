/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

		assertIgnored(m, range0, range1, range2);
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

		assertIgnored(m, range0, range1, range2);
	}

	/**
	 * <pre>
	 * fun exec(block: suspend (p: String) -> Unit): Unit
	 *
	 * fun main() =
	 *     exec { p ->
	 *         suspensionPoint(p)
	 *     }
	 * </pre>
	 *
	 * after <a href=
	 * "https://github.com/JetBrains/kotlin/commit/93782ff35da93ddd603dbf0fac208d7a47593eaa">
	 * change in Kotlin compiler version 2.2</a>
	 */
	@Test
	public void should_filter_suspending_lambdas_with_parameters() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"invokeSuspend", "(Ljava/lang/Object;)Ljava/lang/Object;", null,
				null);

		final Range range1 = new Range();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		range1.fromInclusive = m.instructions.getLast();
		m.visitFieldInsn(Opcodes.GETFIELD, "ExampleKt$main$1", "L$0",
				"Ljava/lang/Object;");
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/intrinsics/IntrinsicsKt",
				"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "ExampleKt$main$1", "label", "I");
		final Label dflt = new Label();
		final Label state0 = new Label();
		final Label state1 = new Label();
		m.visitTableSwitchInsn(0, 1, dflt, state0, state1);

		m.visitLabel(state0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/ResultKt",
				"throwOnFailure", "(Ljava/lang/Object;)V", false);
		range1.toInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitTypeInsn(Opcodes.CHECKCAST, "kotlin/coroutines/Continuation");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/jvm/internal/SpillingKt",
				"nullOutSpilledVariable",
				"(Ljava/lang/Object;)Ljava/lang/Object;", false);
		m.visitFieldInsn(Opcodes.PUTFIELD, "ExampleKt$main$1", "L$0",
				"Ljava/lang/Object;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "ExampleKt$main$1", "label", "I");
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "ExampleKt", "suspensionPoint",
				"(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;",
				false);
		m.visitInsn(Opcodes.DUP);
		final Range range2 = new Range();
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 3);
		final Label label6 = new Label();
		m.visitJumpInsn(Opcodes.IF_ACMPNE, label6);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(state1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/ResultKt",
				"throwOnFailure", "(Ljava/lang/Object;)V", false);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(label6);
		m.visitInsn(Opcodes.POP);
		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(dflt);
		final Range range0 = new Range();
		range0.fromInclusive = m.instructions.getLast();
		m.visitLineNumber(35, dflt);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("call to 'resume' before 'invoke' with coroutine");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IllegalStateException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range0.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(m, range0, range1, range2);
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

		assertIgnored(m, range0, range1, range2);
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

		assertIgnored(m, range1, range2);
	}

	/**
	 * <pre>
	 *     runBlocking {
	 *         // suspending lambda without suspension points,
	 *         // i.e. without invocations of suspending functions/lambdas
	 *     }
	 * </pre>
	 *
	 * https://github.com/JetBrains/kotlin/commit/f4a1e27124f77b2ffca576f7393218373c6ae085
	 *
	 * @see #should_filter_suspending_lambdas()
	 */
	@Test
	public void should_filter_Kotlin_1_6_suspending_lambda_without_suspension_points() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "invokeSuspend",
				"(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/intrinsics/IntrinsicsKt",
				"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
		final Range range1 = new Range();
		range1.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.POP);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "ExampleKt$example$1", "label", "I");

		final Label dflt = new Label();
		final Label state0 = new Label();
		m.visitTableSwitchInsn(0, 0, dflt, state0);

		m.visitLabel(state0);
		{
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/ResultKt",
					"throwOnFailure", "(Ljava/lang/Object;)V", false);
		}
		range1.toInclusive = m.instructions.getLast();

		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
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

		assertIgnored(m, range0, range1);
	}

	/**
	 * <pre>
	 *     fun example() =
	 *         runBlocking {
	 *             ...
	 *             suspensionPointReturningInlineValueClass()
	 *             ...
	 *         }
	 *
	 *     suspend fun suspensionPointReturningInlineValueClass() =
	 *         InlineValueClass("")
	 *
	 *     &#064;kotlin.jvm.JvmInline
	 *     value class InlineValueClass(val value: String)
	 * </pre>
	 */
	@Test
	public void should_filter_suspending_lambdas_and_functions_when_suspension_point_returns_inline_value_class() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "invokeSuspend",
				"(Ljava/lang/Object;)Ljava/lang/Object;", null, null);

		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/intrinsics/IntrinsicsKt",
				"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
		final Range range1 = new Range();
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "ExampleKt$example$1", "label", "I");
		final Label state0 = new Label();
		final Label state1 = new Label();
		final Label dflt = new Label();
		m.visitTableSwitchInsn(0, 1, dflt, state0, state1);
		m.visitLabel(state0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/ResultKt",
				"throwOnFailure", "(Ljava/lang/Object;)V", false);
		range1.toInclusive = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitTypeInsn(Opcodes.CHECKCAST, "kotlin/coroutines/Continuation");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitFieldInsn(Opcodes.PUTFIELD, "ExampleKt$example$1", "label", "I");
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "ExampleKt",
				"suspensionPointReturningInlineValueClass--KaAbg4",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", false);
		m.visitInsn(Opcodes.DUP);

		final Range range2 = new Range();
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 3);
		final Label continuationAfterLoadedResult = new Label();
		m.visitJumpInsn(Opcodes.IF_ACMPNE, continuationAfterLoadedResult);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(state1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/ResultKt",
				"throwOnFailure", "(Ljava/lang/Object;)V", false);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "InlineValueClass");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "InlineValueClass",
				"unbox-impl", "()Ljava/lang/String;", false);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(continuationAfterLoadedResult);

		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		m.visitVarInsn(Opcodes.ASTORE, 2);

		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(dflt);
		final Range range0 = new Range();
		range0.fromInclusive = m.instructions.getLast();
		m.visitLineNumber(29, dflt);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("call to 'resume' before 'invoke' with coroutine");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IllegalStateException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range0.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(m, range0, range1, range2);
	}

	/**
	 * <pre>
	 *     suspend fun example() =
	 *         suspendCoroutine { continuation ->
	 *             ...
	 *         }
	 * </pre>
	 *
	 * @see #should_filter_suspendCoroutineUninterceptedOrReturn_when_no_tail_call_optimization()
	 */
	@Test
	public void should_filter_suspendCoroutineUninterceptedOrReturn() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "example",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", null,
				null);

		m.visitInsn(Opcodes.NOP);

		m.visitInsn(Opcodes.DUP);
		final Range range0 = new Range();
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/intrinsics/IntrinsicsKt",
				"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
		final Label label = new Label();
		m.visitJumpInsn(Opcodes.IF_ACMPNE, label);
		range0.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ALOAD);
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/jvm/internal/DebugProbesKt",
				"probeCoroutineSuspended",
				"(Lkotlin/coroutines/Continuation;)V", false);
		range0.toInclusive = m.instructions.getLast();
		m.visitLabel(label);

		filter.filter(m, context, output);

		assertIgnored(m, range0);
	}

	/**
	 * <pre>
	 *     suspend fun example() {
	 *         suspendCoroutine { continuation ->
	 *             ...
	 *         }
	 *         ...
	 *     }
	 * </pre>
	 *
	 * @see #should_filter_suspendCoroutineUninterceptedOrReturn()
	 */
	@Test
	public void should_filter_suspendCoroutineUninterceptedOrReturn_when_no_tail_call_optimization() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "example",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", null,
				null);

		m.visitInsn(Opcodes.NOP);

		m.visitInsn(Opcodes.DUP);
		final Range range0 = new Range();
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/intrinsics/IntrinsicsKt",
				"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
		final Label label = new Label();
		m.visitJumpInsn(Opcodes.IF_ACMPNE, label);
		range0.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ALOAD);
		m.visitTypeInsn(Opcodes.CHECKCAST, "kotlin/coroutines/Continuation");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/jvm/internal/DebugProbesKt",
				"probeCoroutineSuspended",
				"(Lkotlin/coroutines/Continuation;)V", false);
		range0.toInclusive = m.instructions.getLast();
		m.visitLabel(label);

		filter.filter(m, context, output);

		assertIgnored(m, range0);
	}

}
