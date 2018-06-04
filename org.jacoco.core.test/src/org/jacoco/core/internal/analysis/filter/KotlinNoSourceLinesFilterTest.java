/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nikolay Krasko - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class KotlinNoSourceLinesFilterTest implements IFilterOutput {
	private final static Set<String> KOTLIN_ANNOTATIONS_SET =
			new HashSet<String>(Collections.singletonList(
					KotlinNoSourceLinesFilter.KOTLIN_METADATA_DESC
			));

	private final IFilter filter = new KotlinNoSourceLinesFilter();


	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	@Test
	public void testNoLinesForKotlinWithDebug() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(
				"Foo", "java/lang/Object", KOTLIN_ANNOTATIONS_SET, "data.kt",
				m, this);

		assertMethodSkipped(m);
	}

	@Test
	public void testWithLinesForKotlinWithDebug() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitAnnotation("Lother/Annotation;", false);

		m.visitLineNumber(12, new Label());
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(
				"Foo", "java/lang/Object", KOTLIN_ANNOTATIONS_SET, "data.kt",
				m, this);

		assertNotApplicable();
	}

	@Test
	public void testNoLinesNonKotlinWithDebug() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(
				"Foo", "java/lang/Object",
				Collections.<String>emptySet(), "data.kt",
				m, this);

		assertNotApplicable();
	}

	@Test
	public void testNoLinesForKotlinNoDebug() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(
				"Foo", "java/lang/Object",
				KOTLIN_ANNOTATIONS_SET, null,
				m, this);

		assertNotApplicable();
	}

	@Test
	public void testWithLinesForKotlinNoDebug() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);
		m.visitLineNumber(12, new Label());

		filter.filter(
				"Foo", "java/lang/Object",
				KOTLIN_ANNOTATIONS_SET, null,
				m, this);

		assertNotApplicable();
	}

	private void assertNotApplicable() {
		assertNull(fromInclusive);
		assertNull(toInclusive);
	}

	private void assertMethodSkipped(MethodNode m) {
		assertEquals(m.instructions.getFirst(), fromInclusive);
		assertEquals(m.instructions.getLast(), toInclusive);
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
