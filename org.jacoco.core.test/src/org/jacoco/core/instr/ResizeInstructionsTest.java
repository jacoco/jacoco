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
package org.jacoco.core.instr;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ResizeInstructionsTest {

	private IRuntime runtime;
	private Instrumenter instrumenter;

	private boolean computedCommonSuperClass;

	@Before
	public void setup() throws Exception {
		runtime = new SystemPropertiesRuntime();
		instrumenter = new Instrumenter(runtime);
		runtime.startup(new RuntimeData());
		computedCommonSuperClass = false;
	}

	@After
	public void teardown() {
		runtime.shutdown();
	}

	private class Inner {
	}

	/**
	 * Test of ASM bug
	 * <a href="https://gitlab.ow2.org/asm/asm/issues/317792">#317792</a>.
	 */
	@Test
	public void should_not_loose_InnerClasses_attribute() throws Exception {
		byte[] source = TargetLoader.getClassDataAsBytes(Inner.class);

		final ClassReader cr = InstrSupport.classReaderFor(source);
		final ClassWriter cw = new ClassWriter(0);
		cr.accept(new ClassVisitor(InstrSupport.ASM_API_VERSION, cw) {
			@Override
			public void visitEnd() {
				final MethodVisitor mv = cv.visitMethod(0, "m", "()V", null,
						null);
				mv.visitCode();
				addCauseOfResizeInstructions(mv);
				mv.visitInsn(Opcodes.NOP);
				mv.visitMaxs(2, 1);
				mv.visitEnd();
				super.visitEnd();
			}
		}, 0);
		source = cw.toByteArray();

		final byte[] bytes = instrumenter.instrument(source, "");

		final TargetLoader targetLoader = new TargetLoader();
		final Class<?> outer = targetLoader.add(ResizeInstructionsTest.class,
				TargetLoader.getClassDataAsBytes(ResizeInstructionsTest.class));
		final Class<?> inner = targetLoader.add(Inner.class, bytes);
		assertSame(outer, inner.getEnclosingClass());
		assertNotNull(inner.getEnclosingClass());
		assertSame(outer, inner.getDeclaringClass());
		assertNotNull(inner.getDeclaringClass());
	}

	/**
	 * Test of ASM bug
	 * <a href= "https://gitlab.ow2.org/asm/asm/issues/317630">#317630</a> that
	 * caused {@code java.lang.ClassNotFoundException}.
	 */
	@Test
	public void should_not_require_computation_of_common_superclass()
			throws Exception {
		final String className = "Example";

		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
			@Override
			protected String getCommonSuperClass(final String type1,
					final String type2) {
				computedCommonSuperClass |= className.equals(type1)
						|| className.equals(type2);
				return "java/lang/Object";
			}
		};
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null,
				"java/lang/Object", null);
		final MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "m", "()V",
				null, null);
		mv.visitCode();
		addCauseOfResizeInstructions(mv);
		addCauseOfGetCommonSuperClass(mv);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		cw.visitEnd();
		final byte[] original = cw.toByteArray();
		assertTrue(computedCommonSuperClass);
		new TargetLoader().add(className, original);

		final byte[] instrumented = instrumenter.instrument(original,
				className);
		new TargetLoader().add(className, instrumented);
	}

	/**
	 * Adds code that requires
	 * {@link ClassWriter#getCommonSuperClass(String, String)}.
	 *
	 * <pre>
	 * Object o = this;
	 * while (true) {
	 * 	o = (Integer) null;
	 * }
	 * </pre>
	 */
	private static void addCauseOfGetCommonSuperClass(final MethodVisitor mv) {
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ASTORE, 1);
		Label label = new Label();
		mv.visitLabel(label);
		mv.visitInsn(Opcodes.ACONST_NULL);
		mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
		mv.visitVarInsn(Opcodes.ASTORE, 1);
		mv.visitJumpInsn(Opcodes.GOTO, label);
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

}
