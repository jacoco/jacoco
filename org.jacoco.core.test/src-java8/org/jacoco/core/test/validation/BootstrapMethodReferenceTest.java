/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation;

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
 * Test of ASM bug <a href=
 * "http://forge.ow2.org/tracker/?func=detail&aid=317748&group_id=23&atid=100023">#317748</a>
 * that caused
 * {@code java.lang.ClassFormatError: Short length on BootstrapMethods in class file}
 * during instrumentation.
 */
public class BootstrapMethodReferenceTest {

	private final IRuntime runtime = new SystemPropertiesRuntime();
	private final Instrumenter instrumenter = new Instrumenter(runtime);

	@Before
	public void setup() throws Exception {
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
		return (Integer) new TargetLoader().add(className, bytes)
				.getMethod("run").invoke(null);
	}

	/**
	 * Adds code that triggers usage of
	 * {@link org.objectweb.asm.MethodWriter#INSERTED_FRAMES} during
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

	@SuppressWarnings("unused")
	public static CallSite bootstrap(final MethodHandles.Lookup caller,
			final String name, final MethodType type) throws Exception {
		return new ConstantCallSite(caller.findStatic(BootstrapMethodReferenceTest.class,
				"callTarget", MethodType.methodType(int.class)));
	}

	@SuppressWarnings("unused")
	public static int callTarget() {
		return 42;
	}

}
