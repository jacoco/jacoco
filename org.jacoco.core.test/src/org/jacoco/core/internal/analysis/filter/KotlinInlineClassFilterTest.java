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

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinInlineClassFilter}.
 */
public class KotlinInlineClassFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinInlineClassFilter();

	/**
	 * <pre>
	 * &#064;kotlin.jvm.JvmInline
	 * value class Example(val value: String)
	 * </pre>
	 */
	@Test
	public void should_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.classAnnotations.add("Lkotlin/jvm/JvmInline;");
		final MethodNode m = new MethodNode(0, "getValue",
				"()Ljava/lang/String;", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * &#064;kotlin.jvm.JvmInline
	 * value class Example(val value: String) {
	 *   fun f() { ... }
	 * }
	 * </pre>
	 */
	@Test
	public void should_not_filter_static() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.classAnnotations.add("Lkotlin/jvm/JvmInline;");
		final MethodNode m = new MethodNode(Opcodes.ACC_STATIC, "f-impl", "()V",
				null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	/**
	 * <pre>
	 * data class Example(val value: String)
	 * </pre>
	 */
	@Test
	public void should_not_filter_when_no_JvmInline_annotation() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(0, "getValue",
				"()Ljava/lang/String;", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
