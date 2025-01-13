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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinSafeCallOperatorFilter}.
 */
public class KotlinSafeCallOperatorFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinSafeCallOperatorFilter();

	/**
	 * <pre>
	 * data class A(val b: B)
	 * data class B(val c: String)
	 * fun example(a: A?): String? =
	 *     a?.b?.c
	 * </pre>
	 *
	 * https://github.com/JetBrains/kotlin/commit/0a67ab54fec635f82e0507cbdd4299ae0dbe71b0
	 */
	@Test
	public void should_filter_optimized_safe_call_chain() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(LA;)Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		final Label label1 = new Label();
		final Label label2 = new Label();

		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode ifNullInstruction1 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "A", "getB", "()LB;", false);

		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode ifNullInstruction2 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "B", "getC",
				"()Ljava/lang/String;", false);

		m.visitJumpInsn(Opcodes.GOTO, label2);

		m.visitLabel(label1);
		m.visitInsn(Opcodes.POP);
		final AbstractInsnNode popInstruction = m.instructions.getLast();
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitLabel(label2);

		filter.filter(m, context, output);

		assertIgnored();
		final HashMap<AbstractInsnNode, List<Replacement>> replacements = new HashMap<AbstractInsnNode, List<Replacement>>();
		replacements.put(ifNullInstruction1, Arrays.asList( //
				new Replacement(0, ifNullInstruction2, 0),
				new Replacement(1, popInstruction, 0)));
		replacements.put(ifNullInstruction2, Arrays.asList( //
				new Replacement(0, ifNullInstruction2, 0),
				new Replacement(1, popInstruction, 0)));
		assertReplacedBranches(m, replacements);
	}

	/**
	 * <pre>
	 * data class A(val b: B)
	 * data class B(val c: String)
	 * fun example(a: A?): String? =
	 *     a
	 *         ?.b
	 *         ?.c
	 * </pre>
	 */
	@Test
	public void should_filter_unoptimized_safe_call_chain() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(LA;)Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		final Label label1 = new Label();
		final Label label2 = new Label();

		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode ifNullInstruction1 = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "A", "getB", "()LB;", false);

		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode ifNullInstruction2 = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "B", "getC",
				"()Ljava/lang/String;", false);

		m.visitJumpInsn(Opcodes.GOTO, label2);

		m.visitLabel(label1);
		m.visitInsn(Opcodes.ACONST_NULL);
		final AbstractInsnNode aconstNullInstruction = m.instructions.getLast();
		m.visitLabel(label2);

		filter.filter(m, context, output);

		assertIgnored();
		final HashMap<AbstractInsnNode, List<Replacement>> replacements = new HashMap<AbstractInsnNode, List<Replacement>>();
		replacements.put(ifNullInstruction1, Arrays.asList( //
				new Replacement(0, ifNullInstruction2, 0),
				new Replacement(1, aconstNullInstruction, 0)));
		replacements.put(ifNullInstruction2, Arrays.asList( //
				new Replacement(0, ifNullInstruction2, 0),
				new Replacement(1, aconstNullInstruction, 0)));
		assertReplacedBranches(m, replacements);
	}

}
