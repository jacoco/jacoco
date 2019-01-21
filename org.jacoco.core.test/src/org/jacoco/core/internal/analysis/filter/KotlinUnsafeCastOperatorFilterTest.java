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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinUnsafeCastOperatorFilter}.
 */
public class KotlinUnsafeCastOperatorFilterTest extends FilterTestBase {

	private final KotlinUnsafeCastOperatorFilter filter = new KotlinUnsafeCastOperatorFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	@Test
	public void should_filter() {
		final Label label = new Label();

		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/TypeCastException");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("null cannot be cast to non-null type kotlin.String");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "kotlin/TypeCastException",
				"<init>", "(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

}
