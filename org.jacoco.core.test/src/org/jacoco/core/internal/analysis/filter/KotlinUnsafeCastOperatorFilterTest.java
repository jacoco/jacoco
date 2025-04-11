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
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/TypeCastException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("null cannot be cast to non-null type kotlin.String");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "kotlin/TypeCastException",
				"<init>", "(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);

		filter.filter(m, context, output);

		assertIgnored(m, new Range(expectedFrom, expectedTo));
	}

	@Test
	public void should_filter_Kotlin_1_4() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final Label label = new Label();

		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
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

		assertIgnored(m, new Range(expectedFrom, expectedTo));
	}

	/**
	 * For
	 *
	 * <pre>
	 *   fun f(s: String?): String {
	 * 	   return s as String
	 *   }
	 * </pre>
	 *
	 * bytecode generated by Kotlin compiler version 1.4 is different from
	 * bytecode generated by version 1.5, unfortunately bytecode generated by
	 * later is the same as bytecode that both versions generate for
	 *
	 * <pre>
	 *   fun f(s: String?): String {
	 *     if (s == null)
	 *       throw NullPointerException("null cannot be cast to non-null type kotlin.String")
	 *     return s
	 *   }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		final Label label = new Label();
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("null cannot be cast to non-null type kotlin.String");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/NullPointerException", "<init>",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);
		m.visitVarInsn(Opcodes.ALOAD, 0);

		filter.filter(m, context, output);

		assertIgnored(m, new Range(expectedFrom, expectedTo));
	}

	@Test
	public void should_filter_Kotlin_1_6() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		final Label label = new Label();
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitInsn(Opcodes.POP);
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

		assertIgnored(m, new Range(expectedFrom, expectedTo));
	}

}
