/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jacoco.core.instr.MethodRecorder;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ClassInstrumenter}.
 */
public class ClassInstrumenterTest implements IProbeArrayStrategy {

	private ClassInstrumenter instrumenter;

	@Before
	public void setup() {
		instrumenter = new ClassInstrumenter(this,
				new ClassVisitor(InstrSupport.ASM_API_VERSION) {
				});
	}

	@Test
	public void conditional_helper_should_check_before_storing() {
		for (int access : new int[] { 0, Opcodes.ACC_INTERFACE }) {
			ClassNode node = new ClassNode();
			instrumenter = new ClassInstrumenter(this, node, true);
			instrumenter.visit(Opcodes.V1_8, access, "Foo", null,
					"java/lang/Object", null);
			instrumenter
					.visitMethod(Opcodes.ACC_STATIC, "foo", "()V", null, null)
					.visitProbe(0);
			instrumenter.visitTotalProbeCount(1);
			assertEquals(2, node.methods.size());
			MethodNode helper = node.methods.get(1);
			assertEquals("$jacocoHit", helper.name);
			assertEquals("([ZI)V", helper.desc);
			assertEquals(Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC
					| (access == 0 ? Opcodes.ACC_PRIVATE : Opcodes.ACC_PUBLIC),
					helper.access);
			MethodRecorder actual = new MethodRecorder();
			helper.accept(actual.getVisitor());
			MethodRecorder expected = new MethodRecorder();
			MethodVisitor mv = expected.getVisitor();
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ILOAD, 1);
			mv.visitInsn(Opcodes.BALOAD);
			Label hit = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, hit);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ILOAD, 1);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitInsn(Opcodes.BASTORE);
			mv.visitLabel(hit);
			mv.visitFrame(Opcodes.F_NEW, 2,
					new Object[] { "[Z", Opcodes.INTEGER }, 0, new Object[0]);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(3, 2);
			mv.visitEnd();
			assertEquals(expected, actual);
		}
	}

	@Test
	public void should_not_emit_unused_helper() {
		ClassNode node = new ClassNode();
		instrumenter = new ClassInstrumenter(this, node, true);
		instrumenter.visit(Opcodes.V1_8, 0, "Foo", null, "java/lang/Object",
				null);
		instrumenter.visitTotalProbeCount(0);
		assertEquals(0, node.methods.size());
	}

	@Test
	public void testInstrumentInstrumentedClass1() {
		try {
			instrumenter.visitField(InstrSupport.DATAFIELD_ACC,
					InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC,
					null, null);
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testInstrumentInstrumentedClass2() {
		try {
			instrumenter.visitMethod(InstrSupport.INITMETHOD_ACC,
					InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
					null, null);
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testInstrumentReservedHitMethod() {
		try {
			instrumenter.visitMethod(0, InstrSupport.HITMETHOD_NAME,
					InstrSupport.HITMETHOD_DESC, null, null);
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testNoMethodVisitor() {
		instrumenter = new ClassInstrumenter(this,
				new ClassVisitor(InstrSupport.ASM_API_VERSION) {
					@Override
					public MethodVisitor visitMethod(int access, String name,
							String desc, String signature,
							String[] exceptions) {
						return null;
					}
				});
		assertNull(instrumenter.visitMethod(0, "foo", "()V", null, null));
	}

	// === IProbeArrayStrategy ===

	public int storeInstance(MethodVisitor mv, boolean clinit, int variable) {
		return 0;
	}

	public void addMembers(ClassVisitor cv, int probeCount) {
	}

}
