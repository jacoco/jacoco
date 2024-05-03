/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lukas Rössler - initial implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinSuspendingLambdaFilter}.
 */
public class KotlinSuspendingLambdaFilterTest extends FilterTestBase {

	private final KotlinSuspendingLambdaFilter filter = new KotlinSuspendingLambdaFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"invokeSuspend", "(Ljava/lang/Object;)Ljava/lang/Object", null,
			null);

	/**
	 * <code>
	 * class SuspendingLambda {
	 *     private fun foo(suspendingLambda: suspend () -> Unit) {}
	 *     fun bar() {
	 *         foo {}
	 *     }
	 * }
	 * </code> For this function, an inner class "SuspendingLambda$bar$1" with
	 * an "invokeSuspend" function is created, the byte code of this function is
	 * used for this test case.
	 */
	@Test
	public void should_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		Label l3 = new Label();
		m.visitLabel(l0);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "SuspendingLambda$bar$1", "label",
				"I");
		m.visitTableSwitchInsn(0, 0, l2, l1);
		AbstractInsnNode tableSwitchNode = m.instructions.getLast();
		m.visitLabel(l1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/ResultKt",
				"throwOnFailure", "(Ljava/lang/Object;)V", false);
		m.visitLabel(l3);
		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(l2);
		AbstractInsnNode throwBlockStart = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("call to 'resume' before 'invoke' with coroutine");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IllegalStateException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		AbstractInsnNode throwBlockEnd = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(new Range(tableSwitchNode, tableSwitchNode),
				new Range(throwBlockStart, throwBlockEnd));
	}
}
