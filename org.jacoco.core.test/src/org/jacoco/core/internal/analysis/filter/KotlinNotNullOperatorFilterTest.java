/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinNotNullOperatorFilter}.
 */
public class KotlinNotNullOperatorFilterTest extends FilterTestBase {

	private final KotlinNotNullOperatorFilter filter = new KotlinNotNullOperatorFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"example", "()V", null, null);

	/**
	 * <pre>
	 *     return x!!.length
	 * </pre>
	 */
	@Test
	public void should_filter() {
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.DUP);

		final Range range = new Range();
		final Label label = new Label();
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		range.fromInclusive = m.instructions.getLast();
		// no line number here and hence no probe
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics", "throwNpe", "()V", false);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(label);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length",
				"()I", false);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored(range);
	}

}
