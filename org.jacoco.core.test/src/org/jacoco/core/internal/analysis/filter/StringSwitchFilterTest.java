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
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link StringSwitchFilter}.
 */
public class StringSwitchFilterTest extends FilterTestBase {

	private final IFilter filter = new StringSwitchFilter();

	private final ArrayList<Replacement> replacements = new ArrayList<Replacement>();

	@Test
	public void should_filter() {
		final Range range = new Range();
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);

		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label case3 = new Label();
		final Label caseDefault = new Label();
		final Label h1 = new Label();
		final Label h2 = new Label();

		// filter should not remember this unrelated slot
		m.visitLdcInsn("");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);

		// switch (...)
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		range.fromInclusive = m.instructions.getLast();
		m.visitTableSwitchInsn(97, 98, caseDefault, h1, h2);
		replacements.add(new Replacement(0, m.instructions.getLast(), 0));

		m.visitLabel(h1);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, case1);
		replacements.add(new Replacement(1, m.instructions.getLast(), 1));

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("\0a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "\0a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, case2);
		replacements.add(new Replacement(2, m.instructions.getLast(), 1));

		// goto default case
		m.visitJumpInsn(Opcodes.GOTO, caseDefault);
		replacements.add(new Replacement(0, m.instructions.getLast(), 1));

		m.visitLabel(h2);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "b", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, case3);
		replacements.add(new Replacement(3, m.instructions.getLast(), 1));

		// goto default case
		m.visitJumpInsn(Opcodes.GOTO, caseDefault);
		range.toInclusive = m.instructions.getLast();
		replacements.add(new Replacement(0, m.instructions.getLast(), 1));

		m.visitLabel(case1);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(case2);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(case3);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(caseDefault);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m, range);
		assertReplacedBranches(m, range.fromInclusive.getPrevious(),
				replacements);
	}

	@Test
	public void should_filter_when_default_is_first() {
		final Range range = new Range();
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);

		final Label case1 = new Label();
		final Label caseDefault = new Label();
		final Label h1 = new Label();

		// filter should not remember this unrelated slot
		m.visitLdcInsn("");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);

		// switch (...)
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		range.fromInclusive = m.instructions.getLast();
		m.visitLookupSwitchInsn(caseDefault, new int[] { 97 },
				new Label[] { h1 });
		replacements.add(new Replacement(0, m.instructions.getLast(), 0));

		m.visitLabel(h1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, case1);
		range.toInclusive = m.instructions.getLast();
		replacements.add(new Replacement(1, m.instructions.getLast(), 1));
		replacements.add(new Replacement(0, m.instructions.getLast(), 0));

		m.visitLabel(caseDefault);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(case1);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m, range);
		assertReplacedBranches(m, range.fromInclusive.getPrevious(),
				replacements);
	}

	@Test
	public void should_filter_Kotlin_1_5() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenStringTarget/1.5/example.txt");
		assertSnapshot(filter,
				"snapshots/KotlinWhenStringTarget/1.5/implicit_else.txt");
	}

	@Test
	public void should_filter_Kotlin_biggest_hashCode_first() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenStringTarget/1.5/biggest_hashCode_first.txt");
		assertSnapshot(filter,
				"snapshots/KotlinWhenStringTarget/1.6.20/biggest_hashCode_first.txt");
	}

	@Test
	public void should_filter_Kotlin_nullable_else() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenStringTarget/1.5/nullable_else.txt");
	}

	@Test
	public void should_filter_Kotlin_nullable_case() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenStringTarget/1.5/nullable_case.txt");
		assertSnapshot(filter,
				"snapshots/KotlinWhenStringTarget/1.6.20/nullable_case.txt");
	}

	@Test
	public void should_not_filter_empty_lookup_switch() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "(Ljava/lang/String;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		final Label defaultCase = new Label();
		m.visitLookupSwitchInsn(defaultCase, null, new Label[] {});
		m.visitLabel(defaultCase);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m);
		assertNoReplacedBranches();
	}

}
