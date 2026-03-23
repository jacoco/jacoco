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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link EnumSwitchFilter}.
 */
public class EnumSwitchFilterTest extends FilterTestBase {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private final IFilter filter = new EnumSwitchFilter();

	/**
	 * <pre>
	 * enum E {
	 * 	A, B
	 * }
	 *
	 * int example(E e) {
	 * 	switch (e) {
	 * 	case A:
	 * 		return 1;
	 * 	case B:
	 * 		return 2;
	 * 	}
	 * 	return 0;
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_implicit_default_case_in_inner_enum_switch() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "E", "ordinal", "()I", false);

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();

		m.visitTableSwitchInsn(0, 1, dflt, case1, case2);

		final AbstractInsnNode switchNode = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IRETURN);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitInsn(Opcodes.IRETURN);

		// Default has user code (ICONST_0 + IRETURN), should NOT be ignored
		m.visitLabel(dflt);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertReplacedBranches(m, switchNode,
				Replacements.ignoreDefaultBranch(switchNode));
		// No assertIgnored — default has user code
		assertIgnored(m);
	}

	/**
	 * <pre>
	 * enum E {
	 * 	A, B
	 * }
	 *
	 * // External switch uses simple name mapping often
	 * int example(E e) {
	 * 	switch (e) {
	 * 	case A:
	 * 		return 1;
	 * 	case B:
	 * 		return 2;
	 * 	}
	 * 	return 0;
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_implicit_default_case_in_external_enum_switch() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		// External enum switch often involves a switch map lookup
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example$1", "$SwitchMap$E", "[I");
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "E", "ordinal", "()I", false);
		m.visitInsn(Opcodes.IALOAD);

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();

		m.visitTableSwitchInsn(1, 2, dflt, case1, case2);

		final AbstractInsnNode switchNode = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IRETURN);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitInsn(Opcodes.IRETURN);

		// Default has user code (ICONST_0 + IRETURN), should NOT be ignored
		m.visitLabel(dflt);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertReplacedBranches(m, switchNode,
				Replacements.ignoreDefaultBranch(switchNode));
		assertIgnored(m);
	}

	@Test
	public void should_not_filter_if_not_enum_ordinal() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		// Not ordinal
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "E", "other", "()I", false);

		final Label dflt = new Label();
		final Label case1 = new Label();

		m.visitTableSwitchInsn(0, 0, dflt, case1);

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IRETURN);

		m.visitLabel(dflt);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	@Test
	public void should_not_filter_if_default_in_cases() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "E", "ordinal", "()I", false);

		final Label dflt = new Label(); // Target for default
		final Label case1 = dflt; // Case 1 shares target with default

		m.visitTableSwitchInsn(0, 0, dflt, case1);

		m.visitLabel(dflt);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored(m);

	}

	@Test
	public void should_filter_even_with_intervening_instructions() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "E", "ordinal", "()I", false);

		// Simulating ECJ or other compiler noise
		m.visitInsn(Opcodes.DUP);
		m.visitInsn(Opcodes.POP);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Object");

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();

		m.visitTableSwitchInsn(0, 1, dflt, case1, case2);

		final AbstractInsnNode switchNode = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IRETURN);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitInsn(Opcodes.IRETURN);

		// Default has user code, should NOT be ignored
		m.visitLabel(dflt);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertReplacedBranches(m, switchNode,
				Replacements.ignoreDefaultBranch(switchNode));
		assertIgnored(m);
	}

	/**
	 * ECJ generates INVOKESTATIC $SWITCH_TABLE$() instead of javac's GETSTATIC
	 * $SwitchMap$. Verify the filter handles this pattern.
	 */
	@Test
	public void should_filter_ecj_switch_table_pattern() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		// ECJ pattern: INVOKESTATIC $SWITCH_TABLE$()
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example",
				"$SWITCH_TABLE$com$example$E", "()[I", false);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/example/E", "ordinal",
				"()I", false);
		m.visitInsn(Opcodes.IALOAD);

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label case3 = new Label();

		m.visitTableSwitchInsn(1, 3, dflt, case1, case2, case3);

		final AbstractInsnNode switchNode = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IRETURN);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitInsn(Opcodes.IRETURN);

		m.visitLabel(case3);
		m.visitInsn(Opcodes.ICONST_3);
		m.visitInsn(Opcodes.IRETURN);

		// Default has user code, should NOT be ignored
		m.visitLabel(dflt);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertReplacedBranches(m, switchNode,
				Replacements.ignoreDefaultBranch(switchNode));
		assertIgnored(m);
	}

	/**
	 * When ECJ compiles a switch where all cases use GOTO to common code
	 * (non-returning cases), the implicit default branch is just a GOTO to the
	 * same end point. This single GOTO is a compiler-generated artifact and
	 * should be ignored.
	 */
	@Test
	public void should_ignore_compiler_generated_goto_default() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "E", "ordinal", "()I", false);

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label end = new Label();

		m.visitTableSwitchInsn(0, 1, dflt, case1, case2);

		final AbstractInsnNode switchNode = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, end);

		// Default is just a compiler-generated GOTO — should be ignored
		m.visitLabel(dflt);
		final AbstractInsnNode dfltLabel = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.GOTO, end);
		final AbstractInsnNode dfltGoto = m.instructions.getLast();

		m.visitLabel(end);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertReplacedBranches(m, switchNode,
				Replacements.ignoreDefaultBranch(switchNode));
		assertIgnored(m, new Range(dfltLabel, dfltGoto));
	}

	/**
	 * When the default branch has user code (ICONST, ISTORE, GOTO), it should
	 * NOT be ignored even though it ends with a GOTO.
	 */
	@Test
	public void should_not_ignore_default_with_user_code_and_goto() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()V", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "E", "ordinal", "()I", false);

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label end = new Label();

		m.visitTableSwitchInsn(0, 1, dflt, case1, case2);

		final AbstractInsnNode switchNode = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, end);

		// Default has user code (ICONST_0, ISTORE) before GOTO — NOT ignored
		m.visitLabel(dflt);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(end);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertReplacedBranches(m, switchNode,
				Replacements.ignoreDefaultBranch(switchNode));
		// User code should NOT be ignored
		assertIgnored(m);
	}
}
