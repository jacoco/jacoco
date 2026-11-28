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
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link StringSwitchFilter}.
 */
public class StringSwitchFilterTest extends FilterTestBase {

	private final IFilter filter = new StringSwitchFilter();

	@Test
	public void should_filter() {
		assertSnapshot(filter, "snapshots/StringSwitchTarget/ecj/example.txt");
		assertSnapshot(filter,
				"snapshots/StringSwitchTarget/ecj/few_cases.txt");
	}

	@Test
	public void should_filter_when_default_is_first() {
		assertSnapshot(filter,
				"snapshots/StringSwitchTarget/ecj/default_is_first.txt");
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
