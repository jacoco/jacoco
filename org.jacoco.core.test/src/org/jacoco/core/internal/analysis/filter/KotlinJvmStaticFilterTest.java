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

import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinJvmStaticFilter}.
 */
public class KotlinJvmStaticFilterTest extends FilterTestBase {

	private final KotlinJvmStaticFilter filter = new KotlinJvmStaticFilter();

	/**
	 * <pre>
	 * interface Target {
	 *     companion object {
	 *         &#064;JvmStatic;
	 *         fun target(p: String) = ...
	 *     }
	 * }
	 * </pre>
	 *
	 * <pre>
	 * class Target {
	 *     companion object {
	 *         &#064;JvmStatic;
	 *         fun target() = ...
	 *     }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter() {
		context.className = "Target";
		MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
				"target", "(Ljava/lang/String;)V", null, null);
		m.visitAnnotation("Lkotlin/jvm/JvmStatic;", true);

		// no line number here
		m.visitFieldInsn(Opcodes.GETSTATIC, "Target", "Companion",
				"LTarget$Companion;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Target$Companion", "target",
				"(Ljava/lang/String;)V", false);
		Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(4, label0);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * object Target {
	 *     &#064;JvmStatic;
	 *     fun target() = Other.target()
	 * }
	 *
	 * interface Other {
	 *     companion object {
	 *         &#064;JvmStatic;
	 *         fun target() = ...
	 *     }
	 * }
	 * </pre>
	 */
	@Test
	public void should_not_filter() {
		context.className = "Target";
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC,
				"target", "()V", null, null);
		m.visitAnnotation("Lkotlin/jvm/JvmStatic;", true);

		Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(3, label0);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Other", "Companion",
				"LOther$Companion;");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Other$Companion", "target",
				"()V", false);
		Label label1 = new Label();
		m.visitLabel(label1);
		m.visitLineNumber(3, label1);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

}
