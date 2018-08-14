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

public class AnnotationGeneratedFilterTest implements IFilterOutput {

	private final IFilter filter = new AnnotationGeneratedFilter();

	private final FilterContextMock context = new FilterContextMock();

	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	@Test
	public void should_filter_methods_annotated_with_runtime_visible_org_groovy_transform_Generated() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitAnnotation("Lgroovy/transform/Generated;", true);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, this);

		assertEquals(m.instructions.getFirst(), fromInclusive);
		assertEquals(m.instructions.getLast(), toInclusive);
	}

	@Test
	public void should_filter_methods_annotated_with_runtime_invisible_lombok_Generated() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitAnnotation("Llombok/Generated;", false);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, this);

		assertEquals(m.instructions.getFirst(), fromInclusive);
		assertEquals(m.instructions.getLast(), toInclusive);
	}

	@Test
	public void should_filter_classes_annotated_with_runtime_visible_org_immutables_value_Generated() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		context.classAnnotations.add("Lorg/immutables/value/Generated;");

		filter.filter(m, context, this);

		assertEquals(m.instructions.getFirst(), fromInclusive);
		assertEquals(m.instructions.getLast(), toInclusive);
	}

	@Test
	public void should_filter_when_annotation_is_inner() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		context.classAnnotations.add("Lorg/example/Class$Generated;");

		filter.filter(m, context, this);

		assertEquals(m.instructions.getFirst(), fromInclusive);
		assertEquals(m.instructions.getLast(), toInclusive);
	}

	@Test
	public void should_not_filter_when_no_annotations() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, this);

		assertNull(fromInclusive);
		assertNull(toInclusive);
	}

	@Test
	public void should_not_filter_when_other_annotations() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitAnnotation("LOtherAnnotation;", true);

		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.IRETURN);

		context.classAnnotations.add("LOtherAnnotation;");

		filter.filter(m, context, this);

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
