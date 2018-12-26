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

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"invokeSuspend", "(Ljava/lang/Object;)Ljava/lang/Object;", null,
			null);

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
	public void should_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitLabel(new Label());
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/coroutines/intrinsics/IntrinsicsKt",
				"getCOROUTINE_SUSPENDED", "()Ljava/lang/Object;", false);
		m.visitVarInsn(Opcodes.ASTORE, 4);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		// line of "runBlocking"
		m.visitFieldInsn(Opcodes.GETFIELD, "Target", "label", "I");
		final Label dflt = new Label();
		final Label state0 = new Label();
		final Label state1 = new Label();
		m.visitTableSwitchInsn(0, 1, dflt, state0, state1);
		final Range range1 = new Range();
		range1.fromInclusive = m.instructions.getLast();

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

}
