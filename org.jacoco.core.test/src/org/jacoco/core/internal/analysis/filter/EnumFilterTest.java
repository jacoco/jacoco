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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class EnumFilterTest implements IFilterOutput {

	private final EnumFilter filter = new EnumFilter();

	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	@Test
	public void testValues() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"values", "()[LFoo;", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter("Foo", "java/lang/Enum", m, this);

		assertEquals(m.instructions.getFirst(), fromInclusive);
		assertEquals(m.instructions.getLast(), toInclusive);
	}

	@Test
	public void testNonValues() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"values", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter("Foo", "java/lang/Enum", m, this);

		assertNull(fromInclusive);
		assertNull(toInclusive);
	}

	@Test
	public void testValueOf() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"valueOf", "(Ljava/lang/String;)LFoo;", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter("Foo", "java/lang/Enum", m, this);

		assertEquals(m.instructions.getFirst(), fromInclusive);
		assertEquals(m.instructions.getLast(), toInclusive);
	}

	@Test
	public void testNonValueOf() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"valueOf", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter("Foo", "java/lang/Enum", m, this);

		assertNull(fromInclusive);
		assertNull(toInclusive);
	}

	@Test
	public void testNonEnum() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"values", "()[LFoo;", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter("Foo", "java/lang/Object", m, this);

		assertNull(fromInclusive);
		assertNull(toInclusive);
	}

	public void ignore(final AbstractInsnNode fromInclusive,
			final AbstractInsnNode toInclusive) {
		assertNull(this.fromInclusive);
		this.fromInclusive = fromInclusive;
		this.toInclusive = toInclusive;
	}

	public void merge(final AbstractInsnNode i1, final AbstractInsnNode i2) {
		fail();
	}

}
