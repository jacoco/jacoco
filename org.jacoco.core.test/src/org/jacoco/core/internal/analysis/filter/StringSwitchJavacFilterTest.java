/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
 * Unit tests for {@link StringSwitchJavacFilter}.
 */
public class StringSwitchJavacFilterTest extends FilterTestBase {

	private final IFilter filter = new StringSwitchJavacFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	private AbstractInsnNode expectedFromInclusive;
	private AbstractInsnNode expectedToInclusive;

	private void createFirstSwitch() {
		final Label h1 = new Label();
		final Label h1_2 = new Label();
		final Label h2 = new Label();
		final Label secondSwitch = new Label();

		m.visitInsn(Opcodes.ICONST_M1);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitLookupSwitchInsn(secondSwitch, new int[] { 97, 98 },
				new Label[] { h1, h2 });
		expectedFromInclusive = m.instructions.getLast();

		m.visitLabel(h1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if not equal "a", then goto next comparison
		m.visitJumpInsn(Opcodes.IFEQ, h1_2);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		// goto secondSwitch
		m.visitJumpInsn(Opcodes.GOTO, secondSwitch);

		m.visitLabel(h1_2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("\0a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if not equal "\0a", then goto second switch
		m.visitJumpInsn(Opcodes.IFEQ, secondSwitch);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		// goto secondSwitch
		m.visitJumpInsn(Opcodes.GOTO, secondSwitch);

		m.visitLabel(h2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if not equal "b", then goto second switch
		m.visitJumpInsn(Opcodes.IFEQ, secondSwitch);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		m.visitLabel(secondSwitch);
		expectedToInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ILOAD, 2);
	}

	@Test
	public void should_filter_code_generated_by_javac() {
		createFirstSwitch();

		final Label cases = new Label();
		m.visitTableSwitchInsn(0, 2, cases);
		m.visitLabel(cases);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFromInclusive, expectedToInclusive));
	}

	@Test
	public void should_filter_when_javac_generates_lookupswitch() {
		createFirstSwitch();

		final Label cases = new Label();
		m.visitLookupSwitchInsn(cases, null, new Label[] {});
		m.visitLabel(cases);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFromInclusive, expectedToInclusive));
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

		assertIgnored();
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

		assertIgnored();
	}

}
