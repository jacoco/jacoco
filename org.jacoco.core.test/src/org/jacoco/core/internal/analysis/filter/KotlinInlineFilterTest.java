/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
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

public class KotlinInlineFilterTest extends FilterTestBase {

	@Test
	public void should_filter() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"callsite", "()V", null, null);

		m.visitLineNumber(2, new Label());
		m.visitInsn(Opcodes.NOP);

		final Range range1 = new Range();
		m.visitLineNumber(6, new Label());
		range1.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Stubs", "nop", "()V", false);
		m.visitLineNumber(7, new Label());
		m.visitInsn(Opcodes.NOP);
		range1.toInclusive = m.instructions.getLast();

		m.visitLineNumber(3, new Label());
		m.visitInsn(Opcodes.NOP);

		final Range range2 = new Range();
		m.visitLineNumber(8, new Label());
		range1.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Stubs", "nop", "()V", false);
		m.visitLineNumber(9, new Label());
		m.visitInsn(Opcodes.NOP);
		range1.toInclusive = m.instructions.getLast();

		m.visitLineNumber(4, new Label());
		m.visitInsn(Opcodes.RETURN);

		assertIgnored(range1, range2);
	}

}
