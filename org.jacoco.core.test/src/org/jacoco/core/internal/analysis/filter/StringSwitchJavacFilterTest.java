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
 * Unit tests for {@link StringSwitchJavacFilter}.
 */
public class StringSwitchJavacFilterTest extends FilterTestBase {

	private final IFilter filter = new StringSwitchJavacFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	@Test
	public void should_filter_code_generated_by_javac() {
		assertSnapshot(filter, "snapshots/StringSwitchTarget/example.txt");
		assertSnapshot(filter,
				"snapshots/StringSwitchTarget/default_is_first.txt");
		assertSnapshot(filter, "snapshots/StringSwitchTarget/handwritten.txt");
		assertSnapshot(filter,
				"snapshots/StringSwitchInsideLambdaTarget/example.txt");
		assertSnapshot(filter,
				// https://bugs.openjdk.org/browse/JDK-8372635
				"snapshots/StringSwitchInsideLambdaTarget/24-26/example.txt");
	}

	@Test
	public void should_filter_when_javac_generates_lookupswitch() {
		assertSnapshot(filter, "snapshots/StringSwitchTarget/few_cases.txt");
	}

	/**
	 * <code><pre>
	 * int c = -1;
	 * switch (s.hashCode()) {
	 * case 0:
	 *   if (s.equals(""))
	 *     c = 0;
	 *   return;
	 * default:
	 * }
	 * switch (c)
	 *   // ...
	 * </pre></code>
	 */
	@Test
	public void should_not_filter_when_no_expected_goto() {
		m.visitInsn(Opcodes.ICONST_M1);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);

		final Label secondSwitch = new Label();
		final Label h1 = new Label();
		m.visitTableSwitchInsn(0, 0, secondSwitch, h1);

		m.visitLabel(h1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, secondSwitch);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		// Something different from the expected by filter
		// secondSwitch label or GOTO:
		m.visitInsn(Opcodes.RETURN);

		m.visitLabel(secondSwitch);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		final Label defaultCase = new Label();
		m.visitLookupSwitchInsn(defaultCase, new int[] {}, new Label[] {});
		m.visitLabel(defaultCase);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	@Test
	public void should_not_filter_code_generated_by_ECJ() {
		final Label h1 = new Label();
		final Label h2 = new Label();
		final Label cases = new Label();

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(0, 2, cases, h1, h2);

		m.visitLabel(h1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, cases);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("\0a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "\0a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, cases);

		// goto default case
		m.visitJumpInsn(Opcodes.GOTO, cases);

		m.visitLabel(h2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "b", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, cases);

		// goto default case
		m.visitJumpInsn(Opcodes.GOTO, cases);

		m.visitLabel(cases);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

}
