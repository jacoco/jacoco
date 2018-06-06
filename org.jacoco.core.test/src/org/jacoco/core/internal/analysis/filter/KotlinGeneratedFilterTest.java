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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class KotlinGeneratedFilterTest implements IFilterOutput {

	private final IFilter filter = new KotlinGeneratedFilter();

	private final FilterContextMock context = new FilterContextMock();

	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	@Test
	public void testNoLinesForKotlinWithDebug() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		filter.filter(m, context, this);

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
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		filter.filter(m, context, this);

		assertNotApplicable();
	}

	@Test
	public void testNoLinesNonKotlinWithDebug() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, this);

		assertNotApplicable();
	}

	@Test
	public void testNoLinesForKotlinNoDebug() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.sourceFileName = null;

		filter.filter(m, context, this);

		assertNotApplicable();
	}

	@Test
	public void testWithLinesForKotlinNoDebug() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);
		m.visitLineNumber(12, new Label());
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.sourceFileName = null;

		filter.filter(m, context, this);

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
