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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 * Unit tests for {@link LocalProbeArrayStrategy}.
 */
public class LocalProbeArrayStrategyTest {

	private LocalProbeArrayStrategy strategy;

	@Before
	public void setup() {
		strategy = new LocalProbeArrayStrategy("ClassName", 1L, 2,
				new IExecutionDataAccessorGenerator() {
					public int generateDataAccessor(final long classid,
							final String classname, final int probecount,
							final MethodVisitor mv) {
						assertEquals(1L, classid);
						assertEquals(2, probecount);
						assertEquals("ClassName", classname);
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Runtime",
								"getProbes", "()[Z", false);
						return 42;
					}
				});
	}

	@Test
	public void should_store_instance() {
		final MethodRecorder actualMethod = new MethodRecorder();
		final int maxStack = strategy.storeInstance(actualMethod.getVisitor(),
				false, 13);
		assertEquals(42, maxStack);

		final MethodRecorder expectedMethod = new MethodRecorder();
		final MethodVisitor expected = expectedMethod.getVisitor();
		expected.visitMethodInsn(Opcodes.INVOKESTATIC, "Runtime", "getProbes",
				"()[Z", false);
		expected.visitVarInsn(Opcodes.ASTORE, 13);
		assertEquals(expectedMethod, actualMethod);
	}

	@Test
	public void should_not_add_members() {
		final ClassNode c = new ClassNode();
		strategy.addMembers(c, 0);

		assertEquals(0, c.methods.size());
		assertEquals(0, c.fields.size());
	}

}
