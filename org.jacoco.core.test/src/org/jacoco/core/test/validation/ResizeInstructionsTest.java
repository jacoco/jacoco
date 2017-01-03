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

import static org.junit.Assert.assertTrue;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Test of ASM bug <a href=
 * "http://forge.ow2.org/tracker/?func=detail&aid=317630&group_id=23&atid=100023">#317630</a>
 * that caused {@code java.lang.ClassNotFoundException}.
 */
public class ResizeInstructionsTest {

	private final IRuntime runtime = new SystemPropertiesRuntime();
	private final Instrumenter instrumenter = new Instrumenter(runtime);

	private boolean computedCommonSuperClass = false;

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

}
