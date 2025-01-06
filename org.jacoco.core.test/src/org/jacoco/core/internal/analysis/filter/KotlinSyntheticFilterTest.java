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
 * Unit tests for {@link KotlinSyntheticAccessorsFilter}.
 */
public class KotlinSyntheticFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinSyntheticAccessorsFilter();

	/**
	 * <pre>
	 * &#064;JvmSynthetic
	 * fun example() {
	 * }
	 * </pre>
	 *
	 * @see #should_filter_synthetic_accessor_methods_in_Kotlin_classes()
	 */
	@Test
	public void should_not_filter_synthetic_non_accessor_methods_in_Kotlin_classes() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC, "example", "()V", null, null);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	/**
	 * <pre>
	 * class Outer {
	 *   private var x = 42
	 *
	 *   inner class Inner {
	 *     fun example() {
	 *       x += 1
	 *     }
	 *   }
	 * }
	 * </pre>
	 *
	 * @see #should_not_filter_synthetic_non_accessor_methods_in_Kotlin_classes()
	 */
	@Test
	public void should_filter_synthetic_accessor_methods_in_Kotlin_classes() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC, "access$getX$p", "(Outer;)I", null,
				null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Outer", "x", "I");
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
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
	public void should_not_filter_synthetic_constructor_containing_default_arguments_in_kotlin_classes() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_SYNTHETIC, "<init>",
				"(IILkotlin/jvm/internal/DefaultConstructorMarker;)V", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	/**
	 * For private suspending function Kotlin compiler versions prior to 1.5
	 * produce package-local synthetic method that should not be filtered
	 *
	 * <pre>
	 * private suspend fun example() {
	 * }
	 * </pre>
	 *
	 * @see #should_filter_synthetic_methods_whose_name_starts_with_access_dollar_even_if_last_argument_is_kotlin_coroutine_continuation()
	 */
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

	/**
	 * For private suspending function Kotlin compiler versions starting from
	 * 1.5 produce additional public synthetic method with name starting with
	 * "access$" that should be filtered
	 *
	 * <pre>
	 * private suspend fun example() {
	 * }
	 * </pre>
	 *
	 * @see #should_not_filter_synthetic_methods_whose_last_argument_is_kotlin_coroutine_continuation()
	 */
	@Test
	public void should_filter_synthetic_methods_whose_name_starts_with_access_dollar_even_if_last_argument_is_kotlin_coroutine_continuation() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL
						| Opcodes.ACC_SYNTHETIC,
				"access$example",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "ExampleKt", "example",
				"(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
