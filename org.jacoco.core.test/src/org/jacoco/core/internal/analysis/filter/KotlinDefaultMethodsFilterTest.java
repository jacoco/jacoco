/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link KotlinDefaultMethodsFilter}.
 */
public class KotlinDefaultMethodsFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinDefaultMethodsFilter();

	@Test
	public void should_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"m", "()V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Target$DefaultImpls", "m",
				"(LTarget;)V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_when_invokestatic_owner_does_not_match() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"m", "()V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Target", "m", "(LTarget;)V",
				false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	@Test
	public void should_not_filter_when_instructions_do_not_match() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"m", "()V", null, null);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	/**
	 * <pre>
	 * interface Example {
	 *     fun example(s: String) = s
	 * }
	 * </pre>
	 *
	 * with compiler option <code>-jvm-default=enable</code> which <a href=
	 * "https://github.com/JetBrains/kotlin/commit/4e2914ac5997136bcc8c58b76c8dbef7e2dbe5c0">
	 * became default starting from Kotlin 2.2.0</a>
	 *
	 * @see #should_not_filter_non_compatibility_methods()
	 */
	@Test
	public void should_filter_compatibility_methods() {
		context.className = "Example$DefaultImpls";
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
						| Opcodes.ACC_DEPRECATED,
				"example", "(LExample;Ljava/lang/String;)Ljava/lang/String;",
				null, null);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("s");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics", "checkNotNullParameter",
				"(Ljava/lang/Object;Ljava/lang/String;)V", false);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example", "access$example$jd",
				"(LExample;Ljava/lang/String;)Ljava/lang/String;", true);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * interface Example {
	 *     fun example(s: String) = s
	 * }
	 * </pre>
	 *
	 * with compiler option <code>-jvm-default=disable</code> which <a href=
	 * "https://github.com/JetBrains/kotlin/commit/4e2914ac5997136bcc8c58b76c8dbef7e2dbe5c0">
	 * was default prior to Kotlin 2.2.0</a>
	 *
	 * @see #should_filter_compatibility_methods()
	 */
	@Test
	public void should_not_filter_non_compatibility_methods() {
		context.className = "Example$DefaultImpls";
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "example",
				"(LExample;Ljava/lang/String;)Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("s");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics", "checkNotNullParameter",
				"(Ljava/lang/Object;Ljava/lang/String;)V", false);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

}
