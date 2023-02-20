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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link EnumFilter}.
 */
public class EnumFilterTest extends FilterTestBase {

	private final EnumFilter filter = new EnumFilter();

	@Test
	public void testValues() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"values", "()[LFoo;", null, null);
		m.visitInsn(Opcodes.NOP);
		context.superClassName = "java/lang/Enum";

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testNonValues() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"values", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void testValueOf() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"valueOf", "(Ljava/lang/String;)LFoo;", null, null);
		m.visitInsn(Opcodes.NOP);
		context.superClassName = "java/lang/Enum";

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testNonValueOf() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"valueOf", "()V", null, null);
		m.visitInsn(Opcodes.NOP);
		context.superClassName = "java/lang/Enum";

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void testNonEnum() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"values", "()[LFoo;", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
