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
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link KotlinDefaultArgumentsFilter}.
 */
public class KotlinDefaultArgumentsFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinDefaultArgumentsFilter();

	private static MethodNode createMethod(final int access, final String name,
			final String descriptor) {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				access, name, descriptor, null, null);

		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IAND);
		final Label label = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label);
		// default argument
		m.visitLdcInsn(Integer.valueOf(42));
		m.visitVarInsn(Opcodes.ISTORE, 1);
		m.visitLabel(label);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Target", "origin", "(I)V",
				false);
		m.visitInsn(Opcodes.RETURN);

		return m;
	}

	@Test
	public void should_filter() {
		final MethodNode m = createMethod(Opcodes.ACC_SYNTHETIC,
				"origin$default", "(LTarget;IILjava/lang/Object;)V");
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		filter.filter(m, context, output);

		assertIgnored(new Range(m.instructions.get(3), m.instructions.get(3)));
	}

	@Test
	public void should_not_filter_when_not_kotlin() {
		final MethodNode m = createMethod(Opcodes.ACC_SYNTHETIC,
				"not_kotlin_synthetic$default",
				"(LTarget;IILjava/lang/Object;)V");

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_when_suffix_absent() {
		final MethodNode m = createMethod(Opcodes.ACC_SYNTHETIC,
				"synthetic_without_suffix", "(LTarget;IILjava/lang/Object;)V");
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_when_not_synthetic() {
		final MethodNode m = createMethod(0, "not_synthetic$default",
				"(LTarget;IILjava/lang/Object;)V");
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		filter.filter(m, context, output);

		assertIgnored();
	}

	/**
	 * <pre>
	 * open class Open {
	 *     open fun foo(a: Int = 42) {
	 *     }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_open_functions() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC, "foo$default",
				"(LOpen;IILjava/lang/Object;)V", null, null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		{
			m.visitVarInsn(Opcodes.ALOAD, 3);
			final Label label = new Label();
			m.visitJumpInsn(Opcodes.IFNULL, label);
			m.visitTypeInsn(Opcodes.NEW,
					"java/lang/UnsupportedOperationException");
			m.visitInsn(Opcodes.DUP);
			m.visitLdcInsn(
					"Super calls with default arguments not supported in this target, function: foo");
			m.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"java/lang/UnsupportedOperationException", "<init>",
					"(Ljava/lang/String;)V", false);
			m.visitInsn(Opcodes.ATHROW);
			m.visitLabel(label);
		}
		{
			m.visitVarInsn(Opcodes.ILOAD, 2);
			m.visitInsn(Opcodes.ICONST_1);
			m.visitInsn(Opcodes.IAND);
			final Label label = new Label();
			m.visitJumpInsn(Opcodes.IFEQ, label);
			// default argument
			m.visitLdcInsn(Integer.valueOf(42));
			m.visitVarInsn(Opcodes.ISTORE, 1);
			m.visitLabel(label);

			m.visitVarInsn(Opcodes.ALOAD, 0);
			m.visitVarInsn(Opcodes.ILOAD, 1);
			m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Open", "foo", "(I)V",
					false);
			m.visitInsn(Opcodes.RETURN);
		}

		filter.filter(m, context, output);

		assertIgnored(
				new Range(m.instructions.getFirst(), m.instructions.get(6)),
				new Range(m.instructions.get(11), m.instructions.get(11)));
	}

	/**
	 * <pre>
	 * class C(a: Int = 42)
	 * </pre>
	 */
	@Test
	public void should_filter_constructors() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC, "<init>",
				"(IILkotlin/jvm/internal/DefaultConstructorMarker;)V", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IAND);
		Label label = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label);
		// default argument
		m.visitLdcInsn(Integer.valueOf(42));
		m.visitVarInsn(Opcodes.ISTORE, 1);
		m.visitLabel(label);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Owner", "<init>", "(I)V",
				false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(m.instructions.get(3), m.instructions.get(3)));
	}

	/**
	 * <pre>
	 * data class C(val x: Long = 42)
	 * </pre>
	 */
	@Test
	public void should_filter_methods_with_parameters_that_consume_two_slots() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC, "<init>",
				"(JILkotlin/jvm/internal/DefaultConstructorMarker;)V", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitVarInsn(Opcodes.ILOAD, 3);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IAND);
		final Label label = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label);
		// default argument
		m.visitLdcInsn(Integer.valueOf(42));
		m.visitVarInsn(Opcodes.ISTORE, 1);
		m.visitLabel(label);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Owner", "<init>", "(J)V",
				false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(m.instructions.get(3), m.instructions.get(3)));
	}

}
