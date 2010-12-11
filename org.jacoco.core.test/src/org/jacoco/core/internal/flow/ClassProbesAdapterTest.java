/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * Unit tests for {@link ClassProbesAdapter}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class ClassProbesAdapterTest {

	private static class MockVisitor extends EmptyVisitor implements
			IClassProbesVisitor {

		int count;

		@Override
		public IMethodProbesVisitor visitMethod(int access, String name,
				String desc, String signature, String[] exceptions) {
			return null;
		}

		public void visitTotalProbeCount(int count) {
			this.count = count;
		}
	}

	@Test
	public void testProbeCounter() {
		final MockVisitor mv = new MockVisitor();
		final ClassProbesAdapter adapter = new ClassProbesAdapter(mv);
		assertEquals(0, adapter.nextId());
		assertEquals(1, adapter.nextId());
		assertEquals(2, adapter.nextId());
		adapter.visitEnd();
		assertEquals(3, mv.count);
	}

	@Test
	public void testVisitMethod() {
		final MockVisitor mv = new MockVisitor() {
			@Override
			public IMethodProbesVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				class MockMethodVisitor extends EmptyVisitor implements
						IMethodProbesVisitor {
					public void visitProbe(int probeId) {
					}

					public void visitJumpInsnWithProbe(int opcode, Label label,
							int probeId) {
					}

					public void visitInsnWithProbe(int opcode, int probeId) {
					}

					public void visitTableSwitchInsnWithProbes(int min,
							int max, Label dflt, Label[] labels) {
					}

					public void visitLookupSwitchInsnWithProbes(Label dflt,
							int[] keys, Label[] labels) {
					}
				}
				return new MockMethodVisitor();
			}
		};
		final ClassProbesAdapter adapter = new ClassProbesAdapter(mv);
		writeMethod(adapter);
		writeMethod(adapter);
		writeMethod(adapter);
		adapter.visitEnd();
		assertEquals(3, mv.count);
	}

	@Test
	public void testVisitMethodNullMethodVisitor() {
		final MockVisitor mv = new MockVisitor();
		final ClassProbesAdapter adapter = new ClassProbesAdapter(mv);
		writeMethod(adapter);
		writeMethod(adapter);
		writeMethod(adapter);
		adapter.visitEnd();
		assertEquals(3, mv.count);
	}

	private void writeMethod(final ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(0, "foo", "V()", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 1);
		mv.visitEnd();
	}
}
