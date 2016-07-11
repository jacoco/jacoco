/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.runtime.ModifiedSystemClassRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public class InvokeDynamicProbeArrayStrategyTest {

	private static int access;

	private static boolean[] probes = new boolean[] { false, true };

	@SuppressWarnings("unused")
	public static final Object FIELD = new Object() {
		@Override
		public boolean equals(final Object o) {
			if (o instanceof Object[]) {
				access++;
				Object[] args = (Object[]) o;
				args[0] = probes;
			}
			return false;
		}
	};

	private IExecutionDataAccessorGenerator generator;

	@Before
	public void setup() {
		generator = new ModifiedSystemClassRuntime(
				InvokeDynamicProbeArrayStrategyTest.class, "FIELD");
	}

	@Test
	public void testRuntimeAccess() throws Exception {
		final TargetLoader targetLoader = new TargetLoader();
		final ITarget target = (ITarget) targetLoader.add("Test", create())
				.newInstance();
		final boolean[] probes = target.get();
		target.get2();

		assertSame(InvokeDynamicProbeArrayStrategyTest.probes, probes);
		assertEquals(1, access);
	}

	private byte[] create() {
		final InvokeDynamicProbeArrayStrategy strategy = new InvokeDynamicProbeArrayStrategy(
				"Test", 1, generator);

		final ClassWriter cw = new ClassWriter(0);
		final String outputClassName = "Test";
		cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
				outputClassName, null, "java/lang/Object",
				new String[] { Type.getInternalName(ITarget.class) });

		MethodVisitor mv;

		// Constructor
		GeneratorAdapter gen = new GeneratorAdapter(
				cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null,
						new String[0]),
				Opcodes.ACC_PUBLIC, "<init>", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.invokeConstructor(Type.getType(Object.class),
				new Method("<init>", "()V"));
		gen.returnValue();
		gen.visitMaxs(1, 1);
		gen.visitEnd();

		// get method
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "get", "()[Z", null, null);
		mv.visitCode();
		strategy.storeInstance(mv, 0);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();

		// get2
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "get2", "()[Z", null, null);
		mv.visitCode();
		strategy.storeInstance(mv, 0);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();

		strategy.addMembers(cw, 1);

		cw.visitEnd();

		return cw.toByteArray();
	}

	public interface ITarget {
		/**
		 * Returns a reference to the probe array.
		 *
		 * @return the probe array
		 */
		boolean[] get();

		boolean[] get2();
	}

}
