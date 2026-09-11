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
package org.jacoco.core.test.snapshot;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

/**
 * Unit test for {@link MethodSnapshotVisitor}.
 */
public class MethodSnapshotVisitorTest {

	@Test
	public void delegate() {
		final MethodVisitor delegate = new MethodVisitor(
				InstrSupport.ASM_API_VERSION) {
		};
		final MethodSnapshotVisitor m = new MethodSnapshotVisitor(delegate);
		assertSame(delegate, m.getDelegate());
	}

	@Test
	public void visitTypeAnnotation() {
		assertNull(new MethodSnapshotVisitor(
				new MethodVisitor(InstrSupport.ASM_API_VERSION) {
					@Override
					public AnnotationVisitor visitTypeAnnotation(
							final int typeRef, final TypePath typePath,
							final String descriptor, final boolean visible) {
						throw new AssertionError();
					}
				}).visitTypeAnnotation(
						TypeReference
								.newTypeReference(TypeReference.METHOD_RETURN)
								.getValue(),
						null, "Lorg/jspecify/annotations/Nullable;", true));
	}

	@Test
	public void visitAnnotableParameterCount() {
		new MethodSnapshotVisitor(
				new MethodVisitor(InstrSupport.ASM_API_VERSION) {
					@Override
					public void visitAnnotableParameterCount(
							final int parameterCount, final boolean visible) {
						throw new AssertionError();
					}
				}).visitAnnotableParameterCount(0, true);
	}

	@Test
	public void visitParameterAnnotation() {
		assertNull(new MethodSnapshotVisitor(
				new MethodVisitor(InstrSupport.ASM_API_VERSION) {
					@Override
					public AnnotationVisitor visitParameterAnnotation(
							final int parameter, final String descriptor,
							final boolean visible) {
						throw new AssertionError();
					}
				}).visitParameterAnnotation(0,
						"Lorg/jspecify/annotations/Nullable;", true));
	}

	@Test
	public void visitTryCatchBlock() {
		final MethodSnapshotVisitor m = new MethodSnapshotVisitor(null);
		try {
			m.visitTryCatchBlock(null, null, null, "null");
			fail("UnsupportedOperationException expected");
		} catch (final UnsupportedOperationException e) {
			// expected
		}
	}

	@Test
	public void visitTryCatchAnnotation() {
		assertNull(new MethodSnapshotVisitor(
				new MethodVisitor(InstrSupport.ASM_API_VERSION) {
					@Override
					public AnnotationVisitor visitTryCatchAnnotation(
							final int typeRef, final TypePath typePath,
							final String descriptor, final boolean visible) {
						throw new AssertionError();
					}
				}).visitTryCatchAnnotation(
						TypeReference.newTryCatchReference(0).getValue(), null,
						"Lorg/jspecify/annotations/Nullable;", true));
	}

	@Test
	public void visitLocalVariableAnnotation() {
		assertNull(new MethodSnapshotVisitor(
				new MethodVisitor(InstrSupport.ASM_API_VERSION) {
					@Override
					public AnnotationVisitor visitLocalVariableAnnotation(
							final int typeRef, final TypePath typePath,
							final Label[] start, final Label[] end,
							final int[] index, final String descriptor,
							final boolean visible) {
						throw new AssertionError();
					}
				}).visitLocalVariableAnnotation(
						TypeReference
								.newTypeReference(TypeReference.LOCAL_VARIABLE)
								.getValue(),
						null, null, null, new int[] {},
						"Lorg/jspecify/annotations/Nullable;", true));
	}

}
