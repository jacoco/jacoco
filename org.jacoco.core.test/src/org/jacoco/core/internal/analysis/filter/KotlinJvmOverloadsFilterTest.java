/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinJvmOverloadsFilter}.
 */
public class KotlinJvmOverloadsFilterTest extends FilterTestBase {

	private final KotlinJvmOverloadsFilter filter = new KotlinJvmOverloadsFilter();

	/**
	 * <pre>
	 * &#064;JvmOverloads
	 * fun example(p1: String = "p1") {
	 *   ...
	 * } // line 4
	 * </pre>
	 */
	@Test
	public void should_filter_functions() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "example",
				"()V", null, null);
		m.visitAnnotation("Lkotlin/jvm/JvmOverloads;", false);
		m.visitLabel(new Label()); // lvt label
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example", "example$default",
				"(LExample;Ljava/lang/String;ILjava/lang/Object;)V", false);
		final Label label = new Label();
		m.visitLabel(label);
		m.visitLineNumber(4, label);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(new Label()); // lvt label

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * class Example &#064;JvmOverloads constructor(p1: String = "p1") // line 1
	 * </pre>
	 */
	@Test
	public void should_filter_constructors() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V",
				null, null);
		m.visitAnnotation("Lkotlin/jvm/JvmOverloads;", false);
		m.visitLabel(new Label()); // lvt label
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Example", "<init>",
				"(Ljava/lang/String;ILkotlin/jvm/internal/DefaultConstructorMarker;)V",
				false);
		final Label label = new Label();
		m.visitLabel(label);
		m.visitLineNumber(1, label);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(new Label()); // lvt label

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * fun call() {
	 *   example() // line 2
	 * } // line 3
	 *
	 * &#064;JvmOverloads
	 * fun example(p1: String = "p1") {
	 *   ...
	 * }
	 * </pre>
	 */
	@Test
	public void should_not_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "call", "()V",
				null, null);
		m.visitAnnotation("Lkotlin/jvm/JvmOverloads;", false);
		final Label label1 = new Label();
		m.visitLabel(label1);
		m.visitLineNumber(2, label1);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example", "example$default",
				"(LExample;Ljava/lang/String;ILjava/lang/Object;)V", false);
		final Label label2 = new Label();
		m.visitLabel(label2);
		m.visitLineNumber(3, label2);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(new Label()); // lvt label

		filter.filter(m, context, output);

		assertIgnored();
	}

}
