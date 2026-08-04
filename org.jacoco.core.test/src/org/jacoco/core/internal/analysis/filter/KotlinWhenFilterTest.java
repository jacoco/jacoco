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

import java.util.ArrayList;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinWhenFilter}.
 */
public class KotlinWhenFilterTest extends FilterTestBase {

	private final KotlinWhenFilter filter = new KotlinWhenFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	private final ArrayList<Replacement> replacements = new ArrayList<Replacement>();

	@Test
	public void should_filter_implicit_else() throws Exception {
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/expression.txt");
	}

	@Test
	public void should_not_filter_explicit_else() {
		final Label label = new Label();

		m.visitInsn(Opcodes.NOP);

		m.visitJumpInsn(Opcodes.IFEQ, label);

		m.visitInsn(Opcodes.NOP);

		m.visitLabel(label);
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Throwable");
		m.visitInsn(Opcodes.ATHROW);

		filter.filter(m, context, output);

		assertIgnored(m);
		assertNoReplacedBranches();
	}

	@Test
	public void should_filter_implicit_default() throws Exception {
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/without_else.txt");
	}

	/**
	 * <pre>
	 * enum class E { A, B }
	 * fun example(e: E?) = when (e) {
	 *     E.A -> "a"
	 *     E.B -> "b"
	 *     null -> "null"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_by_nullable_enum_with_null_case_and_without_else()
			throws Exception {
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/nullable_case_without_else.txt");
	}

	/**
	 * <pre>
	 * enum class E { A, B }
	 * fun example(e: E?) = when (e) {
	 *     E.A -> "a"
	 *     E.B -> "b"
	 *     else -> "else"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_by_nullable_enum_without_null_case_and_with_else()
			throws Exception {
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/nullable_else.txt");
	}

	/**
	 * <pre>
	 * enum class E { A, B }
	 * fun example(e: E?) = when (e) {
	 *     E.A -> "a"
	 *     null -> "null"
	 *     else -> "else"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_by_nullable_enum_with_null_and_else_cases()
			throws Exception {
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/nullable_case_with_else.txt");
	}

}
