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
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link ClassFieldProbeArrayStrategy}.
 */
public class ClassFieldProbeArrayStrategyTest {

	private ClassFieldProbeArrayStrategy create(final boolean withFrames) {
		return new ClassFieldProbeArrayStrategy("ClassName", 1L, withFrames,
				new IExecutionDataAccessorGenerator() {
					public int generateDataAccessor(final long classid,
							final String classname, final int probecount,
							final MethodVisitor mv) {
						assertEquals(1L, classid);
						assertEquals("ClassName", classname);
						assertEquals(2, probecount);
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Runtime",
								"getProbes", "()[Z", false);
						return 0;
					}
				});
	}

	@Test
	public void should_add_field_and_init_method() {
		for (final boolean withFrames : new boolean[] { true, false }) {
			final ClassFieldProbeArrayStrategy strategy = create(withFrames);

			final ClassNode c = new ClassNode();
			strategy.addMembers(c, 2);

			assertEquals(1, c.fields.size());
			final FieldNode f = c.fields.get(0);
			assertEquals(
					Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE
							| Opcodes.ACC_STATIC | Opcodes.ACC_TRANSIENT,
					f.access);
			assertEquals("$jacocoData", f.name);
			assertEquals("[Z", f.desc);

			assertEquals(1, c.methods.size());
			final MethodNode m = c.methods.get(0);
			assertEquals(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE
					| Opcodes.ACC_STATIC, m.access);
			assertEquals("$jacocoInit", m.name);
			assertEquals("()[Z", m.desc);
			final MethodRecorder expectedMethod = new MethodRecorder();
			final MethodVisitor expected = expectedMethod.getVisitor();
			expected.visitFieldInsn(Opcodes.GETSTATIC, "ClassName",
					"$jacocoData", "[Z");
			expected.visitInsn(Opcodes.DUP);
			final Label label = new Label();
			expected.visitJumpInsn(Opcodes.IFNONNULL, label);
			expected.visitInsn(Opcodes.POP);
			expected.visitMethodInsn(Opcodes.INVOKESTATIC, "Runtime",
					"getProbes", "()[Z", false);
			expected.visitInsn(Opcodes.DUP);
			expected.visitFieldInsn(Opcodes.PUTSTATIC, "ClassName",
					"$jacocoData", "[Z");
			if (withFrames) {
				expected.visitFrame(Opcodes.F_FULL, 0, new Object[] {}, 1,
						new Object[] { "[Z" });
			}
			expected.visitLabel(label);
			expected.visitInsn(Opcodes.ARETURN);
			expected.visitMaxs(2, 0);

			assertEquals(expectedMethod, MethodRecorder.from(m));
		}
	}

	@Test
	public void should_store_instance() {
		final ClassFieldProbeArrayStrategy strategy = create(true);

		final MethodRecorder actualMethod = new MethodRecorder();
		final int maxStack = strategy.storeInstance(actualMethod.getVisitor(),
				false, 13);
		assertEquals(1, maxStack);

		final MethodRecorder expectedMethod = new MethodRecorder();
		final MethodVisitor expected = expectedMethod.getVisitor();
		expected.visitMethodInsn(Opcodes.INVOKESTATIC, "ClassName",
				"$jacocoInit", "()[Z", false);
		expected.visitVarInsn(Opcodes.ASTORE, 13);
		assertEquals(expectedMethod, actualMethod);
	}

}
