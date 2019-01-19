/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

}
