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
		assertSnapshot(filter, Opcodes.ACC_SYNTHETIC, "f$default",
				"(LTarget;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)V",
				"snapshots/KotlinDefaultArgumentsTarget/1.5/f.txt");

		assertSnapshot(filter, Opcodes.ACC_SYNTHETIC, "branch$default",
				"(LTarget;ZLjava/lang/String;ILjava/lang/Object;)V",
				"snapshots/KotlinDefaultArgumentsTarget/1.5/branch.txt");
	}

	@Test
	public void should_not_filter_when_suffix_absent() {
		final MethodNode m = createMethod(Opcodes.ACC_SYNTHETIC,
				"synthetic_without_suffix", "(LTarget;IILjava/lang/Object;)V");

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	@Test
	public void should_not_filter_when_not_synthetic() {
		final MethodNode m = createMethod(0, "not_synthetic$default",
				"(LTarget;IILjava/lang/Object;)V");

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	@Test
	public void should_filter_open_functions() {
		assertSnapshot(filter, Opcodes.ACC_SYNTHETIC, "foo$default",
				"(LOpen;IILjava/lang/Object;)V",
				"snapshots/KotlinDefaultArgumentsTarget/1.5/open_function.txt");
	}

	@Test
	public void should_filter_constructors() {
		assertSnapshot(filter, Opcodes.ACC_SYNTHETIC, "<init>",
				"(ZLjava/lang/String;ILkotlin/jvm/internal/DefaultConstructorMarker;)V",
				"snapshots/KotlinDefaultArgumentsTarget/1.5/constructor.txt");
	}

	@Test
	public void should_filter_methods_with_parameters_that_consume_two_slots() {
		assertSnapshot(filter, Opcodes.ACC_SYNTHETIC, "<init>",
				"(JILkotlin/jvm/internal/DefaultConstructorMarker;)V",
				"snapshots/KotlinDefaultArgumentsTarget/1.5/long_parameter.txt");
	}

	/**
	 * <pre>
	 * class C(
	 *   p1: Int,
	 *   ...
	 *   p31: Int,
	 *   p32: Int = 42,
	 *   p33: Int = 42,
	 * )
	 * </pre>
	 *
	 * This constructor will have 2 additional parameters containing the mask.
	 */
	@Test
	public void should_filter_methods_with_more_than_32_parameters() {
		final StringBuilder paramTypes = new StringBuilder();
		for (int i = 1; i <= 33; i++) {
			paramTypes.append("Ljava/lang/String;");
		}
		assertSnapshot(filter, Opcodes.ACC_SYNTHETIC, "<init>",
				"(" + paramTypes
						+ "IILkotlin/jvm/internal/DefaultConstructorMarker;)V",
				"snapshots/KotlinDefaultArgumentsTarget/1.5/more_than_32_parameters.txt");
	}

	/**
	 * <pre>
	 * class C(
	 *   p1: Int = 42,
	 *   p2: Int,
	 *   ...
	 *   p224: Int,
	 *   p225: Int = 42,
	 * )
	 * </pre>
	 *
	 * This constructor will have 8 additional parameters containing the mask.
	 *
	 * 9 additional parameters will be required for methods with more than 256
	 * parameters, but according to Java Virtual Machine Specification <a href=
	 * "https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.11">
	 * §4.11</a>:
	 *
	 * <blockquote>
	 * <p>
	 * The number of method parameters is limited to 255
	 * </p>
	 * </blockquote>
	 */
	@Test
	public void should_filter_methods_with_more_than_224_parameters() {
		final StringBuilder paramTypes = new StringBuilder();
		for (int i = 1; i <= 225; i++) {
			paramTypes.append("I");
		}
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC, "<init>",
				"(" + paramTypes
						+ "IIIIIIIILkotlin/jvm/internal/DefaultConstructorMarker;)V",
				null, null);

		m.visitVarInsn(Opcodes.ILOAD, 226);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IAND);
		final Label label = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label);
		final Range range1 = new Range(m.instructions.getLast(),
				m.instructions.getLast());
		// default argument
		m.visitLdcInsn(Integer.valueOf(42));
		m.visitVarInsn(Opcodes.ISTORE, 1);
		m.visitLabel(label);

		m.visitVarInsn(Opcodes.ILOAD, 233);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IAND);
		final Label label2 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label2);
		final Range range2 = new Range(m.instructions.getLast(),
				m.instructions.getLast());
		// default argument
		m.visitLdcInsn(Integer.valueOf(42));
		m.visitVarInsn(Opcodes.ISTORE, 225);
		m.visitLabel(label2);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		for (int i = 1; i <= 225; i++) {
			m.visitVarInsn(Opcodes.ILOAD, i);
		}
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Owner", "<init>",
				"(" + paramTypes + ")V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m, range1, range2);
	}

	@Test
	public void computeNumberOfMaskArguments() {
		assertEquals(1,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(3));
		assertEquals(1,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(34));
		assertEquals(2,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(36));
		assertEquals(2,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(67));
		assertEquals(3,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(69));
		assertEquals(3,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(100));
		assertEquals(4,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(102));
		assertEquals(4,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(133));
		assertEquals(5,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(135));
		assertEquals(5,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(166));
		assertEquals(6,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(168));
		assertEquals(6,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(199));
		assertEquals(7,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(201));
		assertEquals(7,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(232));
		assertEquals(8,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(234));
		assertEquals(8,
				KotlinDefaultArgumentsFilter.computeNumberOfMaskArguments(255));
	}

}
