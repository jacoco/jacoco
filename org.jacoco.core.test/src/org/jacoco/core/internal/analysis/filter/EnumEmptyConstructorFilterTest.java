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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link EnumEmptyConstructorFilter}.
 */
public class EnumEmptyConstructorFilterTest extends FilterTestBase {

	private final EnumEmptyConstructorFilter filter = new EnumEmptyConstructorFilter();

	@Test
	public void should_filter() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V", null,
				null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>",
				"(Ljava/lang/String;I)V", false);
		m.visitInsn(Opcodes.RETURN);
		context.superClassName = "java/lang/Enum";

		filter.filter(m, context, output);

		assertIgnored(
				new Range(m.instructions.getFirst(), m.instructions.getLast()));
	}

	/**
	 * <code><pre>
	 * enum E {
	 *   ;
	 *   private E() {
	 *     ...
	 *   }
	 * }
	 * </pre></code>
	 */
	@Test
	public void should_not_filter_non_empty_constructor() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V", null,
				null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>",
				"(Ljava/lang/String;I)V", false);
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.RETURN);
		context.superClassName = "java/lang/Enum";

		filter.filter(m, context, output);

		assertIgnored();
	}

	/**
	 * <code><pre>
	 * enum E {
	 *   ;
	 *   private E(long p) {
	 *   }
	 * }
	 * </pre></code>
	 */
	@Test
	public void should_not_filter_constructor_with_additional_parameters() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "(Ljava/lang/String;IJ)V", null,
				null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>",
				"(Ljava/lang/String;I)V", false);
		m.visitInsn(Opcodes.RETURN);
		context.superClassName = "java/lang/Enum";

		filter.filter(m, context, output);

		assertIgnored();
	}

	/**
	 * <code><pre>
	 * enum E {
	 *   ;
	 *   private void method(String p1, int p2) {
	 *   }
	 * }
	 * </pre></code>
	 */
	@Test
	public void should_not_filter_non_constructor() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "method", "(Ljava/lang/String;I)V", null,
				null);
		m.visitInsn(Opcodes.NOP);
		context.superClassName = "java/lang/Enum";

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_non_Enum() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V", null,
				null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
