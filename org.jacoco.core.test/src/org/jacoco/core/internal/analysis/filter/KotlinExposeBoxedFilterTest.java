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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link KotlinExposeBoxedFilter}.
 */
public class KotlinExposeBoxedFilterTest extends FilterTestBase {

	private final KotlinExposeBoxedFilter filter = new KotlinExposeBoxedFilter();

	/**
	 * <pre>
	 * &#064;JvmExposeBoxed
	 * fun example(v: ValueClass) = ...
	 * </pre>
	 */
	@Test
	public void should_filter_when_parameter_exposed() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()LValueClass;", null, null);
		m.visitAnnotation("Lkotlin/jvm/JvmExposeBoxed;", false);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ValueClass", "unbox-impl",
				"()Ljava/lang/String;", false);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * &#064;JvmExposeBoxed(jvmName = "exposed")
	 * fun example(): ValueClass = ...
	 * </pre>
	 */
	@Test
	public void should_filter_when_return_type_exposed() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"exposed", "()LValueClass;", null, null);
		m.visitAnnotation("Lkotlin/jvm/JvmExposeBoxed;", false);

		m.visitMethodInsn(Opcodes.INVOKESTATIC, "ValueClass", "box-impl",
				"(Ljava/lang/String;)LValueClass;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * &#064;JvmExposeBoxed
	 * fun example() = ...
	 * </pre>
	 */
	@Test
	public void should_not_filter_when_nothing_exposed() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);
		m.visitAnnotation("Lkotlin/jvm/JvmExposeBoxed;", false);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	/**
	 * @see #should_filter_when_parameter_exposed()
	 */
	@Test
	public void should_not_filter_when_no_annotation() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()LValueClass;", null, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ValueClass", "unbox-impl",
				"()Ljava/lang/String;", false);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

}
