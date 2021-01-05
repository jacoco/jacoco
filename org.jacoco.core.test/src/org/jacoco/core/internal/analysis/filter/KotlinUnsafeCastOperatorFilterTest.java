/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinUnsafeCastOperatorFilter}.
 */
public class KotlinUnsafeCastOperatorFilterTest extends FilterTestBase {

	private final KotlinUnsafeCastOperatorFilter filter = new KotlinUnsafeCastOperatorFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	@Test
	public void should_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final Label label = new Label();

		m.visitInsn(Opcodes.DUP);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		m.visitTypeInsn(Opcodes.NEW, "kotlin/TypeCastException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("null cannot be cast to non-null type kotlin.String");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "kotlin/TypeCastException",
				"<init>", "(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	@Test
	public void should_filter_Kotlin_1_4() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final Label label = new Label();

		m.visitInsn(Opcodes.DUP);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("null cannot be cast to non-null type kotlin.String");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/NullPointerException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 *   fun f(o: Any?) {
	 *     if (o == null)
	 *       throw NullPointerException("null cannot be cast to non-null type")
	 *   }
	 * </pre>
	 */
	@Test
	public void should_not_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		final Label label = new Label();
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("null cannot be cast to non-null type");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/NullPointerException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(label);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_when_not_kotlin() {
		m.visitInsn(Opcodes.DUP);
		final Label label = new Label();
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("null cannot be cast to non-null type kotlin.String");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/NullPointerException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(label);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
