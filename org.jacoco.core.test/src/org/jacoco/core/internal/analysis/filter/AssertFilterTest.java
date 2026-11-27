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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link AssertFilter}.
 */
public class AssertFilterTest extends FilterTestBase {

	private final AssertFilter filter = new AssertFilter();

	@Test
	public void should_filter_static_initializer() {
		context.className = "org/jacoco/core/test/validation/java5/targets/AssertTarget";
		assertSnapshot(filter, "<clinit>", "()V",
				"snapshots/AssertTarget/clinit.txt");
	}

	/**
	 * <code><pre>
	 * class Example {
	 *   static final f;
	 *
	 *   static {
	 *     f = !Example.class.desiredAssertionStatus();
	 *   }
	 * }
	 * </pre></code>
	 */
	@Test
	public void should_not_filter_static_initializer_when_field_name_does_not_match() {
		context.className = "Example";
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);

		m.visitLdcInsn(Type.getType("LExample;"));
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
				"desiredAssertionStatus", "()Z", false);
		final Label label1 = new Label();
		final Label label2 = new Label();
		m.visitJumpInsn(Opcodes.IFNE, label1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitJumpInsn(Opcodes.GOTO, label2);
		m.visitLabel(label1);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitLabel(label2);
		m.visitFieldInsn(Opcodes.PUTSTATIC, "Foo", "f", "Z");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	@Test
	public void should_filter_assert() {
		context.className = "org/jacoco/core/test/validation/java5/targets/AssertTarget";
		assertSnapshot(filter, "snapshots/AssertTarget/assert.txt");
	}

}
