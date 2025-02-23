/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.instr.MethodRecorder;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link InterfaceFieldProbeArrayStrategy}.
 */
public class InterfaceFieldProbeArrayStrategyTest {

	private InterfaceFieldProbeArrayStrategy strategy;

	@Before
	public void setup() {
		strategy = new InterfaceFieldProbeArrayStrategy("ClassName", 1L, 2,
				new IExecutionDataAccessorGenerator() {
					public int generateDataAccessor(final long classid,
							final String classname, final int probecount,
							final MethodVisitor mv) {
						assertEquals(1L, classid);
						assertEquals("ClassName", classname);
						assertEquals(2, probecount);
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Runtime",
								"getProbes", "()[Z", false);
						return 1;
					}
				});
	}

	@Test
	public void should_add_field() {
		final ClassNode c = new ClassNode();
		strategy.addMembers(c, 2);

		assertEquals(1, c.fields.size());
		final FieldNode f = c.fields.get(0);
		assertEquals(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC
				| Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, f.access);
		assertEquals("$jacocoData", f.name);
		assertEquals("[Z", f.desc);
	}

	@Test
	public void should_store_instance_in_non_clinit_methods() {
		final MethodRecorder actualMethod = new MethodRecorder();
		final int maxStack = strategy.storeInstance(actualMethod.getVisitor(),
				false, 1);
		assertEquals(1, maxStack);

		final MethodRecorder expectedMethod = new MethodRecorder();
		final MethodVisitor expected = expectedMethod.getVisitor();
		expected.visitMethodInsn(Opcodes.INVOKESTATIC, "ClassName",
				"$jacocoInit", "()[Z", true);
		expected.visitVarInsn(Opcodes.ASTORE, 1);
		assertEquals(expectedMethod, actualMethod);
	}

	@Test
	public void should_add_init_and_clinit_methods() {
		final ClassNode c = new ClassNode();
		strategy.storeInstance(
				c.visitMethod(Opcodes.ACC_PUBLIC, "name", "()V", null, null),
				false, 1);
		strategy.addMembers(c, 2);

		assertEquals(3, c.methods.size());

		assertInitMethod(c.methods.get(1));

		final MethodNode m = c.methods.get(2);
		assertEquals(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC, m.access);
		assertEquals("<clinit>", m.name);
		assertEquals("()V", m.desc);
		final MethodRecorder expectedMethod = new MethodRecorder();
		final MethodVisitor expected = expectedMethod.getVisitor();
		expected.visitMethodInsn(Opcodes.INVOKESTATIC, "Runtime", "getProbes",
				"()[Z", false);
		expected.visitFieldInsn(Opcodes.PUTSTATIC, "ClassName", "$jacocoData",
				"[Z");
		expected.visitInsn(Opcodes.RETURN);
		expected.visitMaxs(1, 0);
		assertEquals(expectedMethod, MethodRecorder.from(m));
	}

	@Test
	public void should_add_init_method_and_update_existing_clinit_method() {
		final ClassNode c = new ClassNode();
		final int maxStack = strategy.storeInstance(
				c.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
						"<clinit>", "()V", null, null),
				true, 1);
		assertEquals(2, maxStack);
		strategy.addMembers(c, 2);

		assertEquals(2, c.methods.size());

		assertInitMethod(c.methods.get(1));

		final MethodRecorder expectedMethod = new MethodRecorder();
		final MethodVisitor expected = expectedMethod.getVisitor();
		expected.visitMethodInsn(Opcodes.INVOKESTATIC, "Runtime", "getProbes",
				"()[Z", false);
		expected.visitInsn(Opcodes.DUP);
		expected.visitFieldInsn(Opcodes.PUTSTATIC, "ClassName", "$jacocoData",
				"[Z");
		expected.visitVarInsn(Opcodes.ASTORE, 1);
		expected.visitMaxs(0, 0);
		assertEquals(expectedMethod, MethodRecorder.from(c.methods.get(0)));
	}

	private static void assertInitMethod(final MethodNode m) {
		assertEquals(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE
				| Opcodes.ACC_STATIC, m.access);
		assertEquals("$jacocoInit", m.name);
		assertEquals("()[Z", m.desc);
		final MethodRecorder expectedMethod = new MethodRecorder();
		final MethodVisitor expected = expectedMethod.getVisitor();
		expected.visitFieldInsn(Opcodes.GETSTATIC, "ClassName", "$jacocoData",
				"[Z");
		expected.visitInsn(Opcodes.DUP);
		final Label label = new Label();
		expected.visitJumpInsn(Opcodes.IFNONNULL, label);
		expected.visitInsn(Opcodes.POP);
		expected.visitMethodInsn(Opcodes.INVOKESTATIC, "Runtime", "getProbes",
				"()[Z", false);
		expected.visitFrame(Opcodes.F_FULL, 0, new Object[] {}, 1,
				new Object[] { "[Z" });
		expected.visitLabel(label);
		expected.visitInsn(Opcodes.ARETURN);
		expected.visitMaxs(2, 0);
		assertEquals(expectedMethod, MethodRecorder.from(m));
	}

}
