/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link ClassProbesAdapter}.
 */
public class ClassProbesAdapterTest {

	private static class MockMethodVisitor extends MethodProbesVisitor {

		boolean frame = false;

		@Override
		public void visitJumpInsnWithProbe(int opcode, Label label, int probeId,
				IFrame frame) {
			frame.accept(this);
		}

		@Override
		public void visitTableSwitchInsnWithProbes(int min, int max, Label dflt,
				Label[] labels, IFrame frame) {
			frame.accept(this);
		}

		@Override
		public void visitLookupSwitchInsnWithProbes(Label dflt, int[] keys,
				Label[] labels, IFrame frame) {
			frame.accept(this);
		}

		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack,
				Object[] stack) {
			frame = true;
		}
	}

	private static class MockClassVisitor extends ClassProbesVisitor {

		int count;

		@Override
		public MethodProbesVisitor visitMethod(int access, String name,
				String desc, String signature, String[] exceptions) {
			return null;
		}

		@Override
		public void visitTotalProbeCount(int count) {
			this.count = count;
		}
	}

	@Test
	public void testProbeCounter() {
		final MockClassVisitor cv = new MockClassVisitor();
		final ClassProbesAdapter adapter = new ClassProbesAdapter(cv, false);
		assertEquals(0, adapter.nextId());
		assertEquals(1, adapter.nextId());
		assertEquals(2, adapter.nextId());
		adapter.visitEnd();
		assertEquals(3, cv.count);
	}

	@Test
	public void testVisitClassMethods() {
		final MockClassVisitor cv = new MockClassVisitor() {
			@Override
			public MethodProbesVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				return new MockMethodVisitor();
			}
		};
		final ClassProbesAdapter adapter = new ClassProbesAdapter(cv, false);
		adapter.visit(Opcodes.V1_5, 0, "Foo", null, "java/lang/Object", null);
		writeMethod(adapter);
		writeMethod(adapter);
		writeMethod(adapter);

		assertEquals(0, cv.count);
		adapter.visitEnd();
		assertEquals(3, cv.count);
	}

	@Test
	public void testVisitMethodNullMethodVisitor() {
		final MockClassVisitor cv = new MockClassVisitor();
		final ClassProbesAdapter adapter = new ClassProbesAdapter(cv, false);
		writeMethod(adapter); // 1 probe
		writeMethodWithBranch(adapter); // 3 probes
		writeMethodWithTableSwitch(adapter); // 3 probes
		writeMethodWithLookupSwitch(adapter); // 3 probes
		adapter.visitEnd();
		assertEquals(10, cv.count);
	}

	@Test
	public void testVisitWithFrames() {
		final MockMethodVisitor mv = new MockMethodVisitor();
		final MockClassVisitor cv = new MockClassVisitor() {
			@Override
			public MethodProbesVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				return mv;
			}
		};
		final ClassProbesAdapter adapter = new ClassProbesAdapter(cv, true);
		writeMethodWithBranch(adapter);
		adapter.visitEnd();
		assertTrue(mv.frame);
	}

	@Test
	public void testVisitWithoutFrames() {
		final MockMethodVisitor mv = new MockMethodVisitor();
		final MockClassVisitor cv = new MockClassVisitor() {
			@Override
			public MethodProbesVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				return mv;
			}
		};
		final ClassProbesAdapter adapter = new ClassProbesAdapter(cv, false);
		writeMethodWithBranch(adapter);
		adapter.visitEnd();
		assertFalse(mv.frame);
	}

	private void writeMethod(final ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(0, "foo", "()V", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 1);
		mv.visitEnd();
	}

	private void writeMethodWithBranch(final ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(0, "foo", "()V", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.ICONST_0);
		Label l = new Label();
		mv.visitJumpInsn(Opcodes.IFEQ, l);
		mv.visitInsn(Opcodes.NOP);
		mv.visitLabel(l);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void writeMethodWithTableSwitch(final ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(0, "foo", "()V", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.ICONST_0);
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTableSwitchInsn(0, 0, l1, new Label[] { l2 });
		mv.visitLabel(l1);
		mv.visitInsn(Opcodes.NOP);
		mv.visitLabel(l2);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void writeMethodWithLookupSwitch(final ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(0, "foo", "()V", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.ICONST_0);
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitLookupSwitchInsn(l1, new int[] { 0 }, new Label[] { l2 });
		mv.visitLabel(l1);
		mv.visitInsn(Opcodes.NOP);
		mv.visitLabel(l2);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

}
