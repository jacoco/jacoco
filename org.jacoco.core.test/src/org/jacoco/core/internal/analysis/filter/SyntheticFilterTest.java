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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link SyntheticFilter}.
 */
public class SyntheticFilterTest extends FilterTestBase {

	private final SyntheticFilter filter = new SyntheticFilter();

	@Test
	public void testNonSynthetic() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void testSynthetic() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC, "name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testLambda() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC, "lambda$1", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_method_with_suffix_default_in_kotlin_classes() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE, "example$default",
				"(LTarget;Ljava/lang/String;Ijava/lang/Object;)V", null, null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_filter_synthetic_method_with_suffix_default_in_non_kotlin_classes() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE, "example$default",
				"(LTarget;Ljava/lang/String;Ijava/lang/Object;)V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_synthetic_methods_whose_last_argument_is_kotlin_coroutine_continuation() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC, "example",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
