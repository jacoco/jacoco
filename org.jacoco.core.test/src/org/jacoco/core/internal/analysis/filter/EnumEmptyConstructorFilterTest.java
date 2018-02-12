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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class EnumEmptyConstructorFilterTest implements IFilterOutput {

	private final EnumEmptyConstructorFilter filter = new EnumEmptyConstructorFilter();

	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	@Test
	public void should_filter() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V", null,
				null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>",
				"(Ljava/lang/String;I)V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter("Foo", "java/lang/Enum", m, this);

		assertEquals(m.instructions.getFirst(), fromInclusive);
		assertEquals(m.instructions.getLast(), toInclusive);
	}

	/**
	 * <code><pre>
	 * enum E {
	 *   ;
	 *   private E() {
	 *     ...
	 *   }
	 * }
	 * </pre></code>
	 */
	@Test
	public void should_not_filter_non_empty_constructor() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V", null,
				null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>",
				"(Ljava/lang/String;I)V", false);
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.RETURN);

		filter.filter("Foo", "java/lang/Enum", m, this);

		assertNull(fromInclusive);
		assertNull(toInclusive);
	}

	/**
	 * <code><pre>
	 * enum E {
	 *   ;
	 *   private E(long p) {
	 *   }
	 * }
	 * </pre></code>
	 */
	@Test
	public void should_not_filter_constructor_with_additional_parameters() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "(Ljava/lang/String;IJ)V", null,
				null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>",
				"(Ljava/lang/String;I)V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter("Foo", "java/lang/Enum", m, this);

		assertNull(fromInclusive);
		assertNull(toInclusive);
	}

	/**
	 * <code><pre>
	 * enum E {
	 *   ;
	 *   private void method(String p1, int p2) {
	 *   }
	 * }
	 * </pre></code>
	 */
	@Test
	public void should_not_filter_non_constructor() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "method", "(Ljava/lang/String;I)V", null,
				null);
		m.visitInsn(Opcodes.NOP);

		filter.filter("Foo", "java/lang/Enum", m, this);

		assertNull(fromInclusive);
		assertNull(toInclusive);
	}

	@Test
	public void should_not_filter_non_Enum() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V", null,
				null);
		m.visitInsn(Opcodes.NOP);

		filter.filter("Foo", "java/lang/Object", m, this);

		assertNull(fromInclusive);
		assertNull(toInclusive);
	}

	public void ignore(AbstractInsnNode fromInclusive,
			AbstractInsnNode toInclusive) {
		assertNull(this.fromInclusive);
		this.fromInclusive = fromInclusive;
		this.toInclusive = toInclusive;
	}

	public void merge(AbstractInsnNode i1, AbstractInsnNode i2) {
		fail();
	}

}
