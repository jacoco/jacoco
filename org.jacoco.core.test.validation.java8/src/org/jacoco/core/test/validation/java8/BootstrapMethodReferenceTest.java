/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.java8;

import static org.junit.Assert.assertEquals;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Test of ASM bug
 * <a href="https://gitlab.ow2.org/asm/asm/issues/317748">#317748</a> that
 * caused
 * {@code java.lang.ClassFormatError: Short length on BootstrapMethods in class file}
 * during instrumentation.
 */
public class BootstrapMethodReferenceTest {

	private IRuntime runtime;
	private Instrumenter instrumenter;

	@Before
	public void setup() throws Exception {
		runtime = new SystemPropertiesRuntime();
		instrumenter = new Instrumenter(runtime);
		runtime.startup(new RuntimeData());
	}

	@After
	public void teardown() {
		runtime.shutdown();
	}

	@Test
	public void test() throws Exception {
		final String className = "Example";

		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, className, null,
				"java/lang/Object", null);

		final MethodVisitor mv = cw.visitMethod(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()I", null,
				null);
		mv.visitCode();
		addCauseOfResizeInstructions(mv);
		final MethodType methodType = MethodType.methodType(CallSite.class,
				MethodHandles.Lookup.class, String.class, MethodType.class);
		final Handle handle = new Handle(Opcodes.H_INVOKESTATIC,
				this.getClass().getCanonicalName().replace('.', '/'),
				"bootstrap", methodType.toMethodDescriptorString(), false);
		mv.visitInvokeDynamicInsn("invoke", "()I", handle);
		mv.visitInsn(Opcodes.IRETURN);
		mv.visitMaxs(1, 0);
		mv.visitEnd();

		cw.visitEnd();

		final byte[] original = cw.toByteArray();
		assertEquals(42, run(className, original));

		final byte[] instrumented = instrumenter.instrument(original,
				className);
		assertEquals(42, run(className, instrumented));
	}

	private static int run(final String className, final byte[] bytes)
			throws ClassNotFoundException, NoSuchMethodException,
			InvocationTargetException, IllegalAccessException {
		final Integer result = (Integer) new TargetLoader()
				.add(className, bytes).getMethod("run").invoke(null);
		return result.intValue();
	}

	/**
	 * Adds code that triggers usage of
	 * {@link org.objectweb.asm.MethodWriter#COMPUTE_INSERTED_FRAMES} during
	 * instrumentation.
	 */
	private static void addCauseOfResizeInstructions(final MethodVisitor mv) {
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.ICONST_1);
		final Label target = new Label();
		mv.visitJumpInsn(Opcodes.IFLE, target);
		for (int i = 0; i < Short.MAX_VALUE; i++) {
			mv.visitInsn(Opcodes.NOP);
		}
		mv.visitLabel(target);
	}

	public static CallSite bootstrap(final MethodHandles.Lookup caller,
			final String name, final MethodType type) throws Exception {
		return new ConstantCallSite(
				caller.findStatic(BootstrapMethodReferenceTest.class,
						"callTarget", MethodType.methodType(int.class)));
	}

	public static int callTarget() {
		return 42;
	}

}
