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
package org.jacoco.core.internal.instr;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.JavaVersion;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This test verifies that instrumentation can handle case when the last local
 * variable of method parameters is overridden in the method body to store
 * <a href=
 * "https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.10.2.3">a
 * value of type long or double which occupy two variables</a>.
 *
 * @see ProbeInserterTest#visitFrame_should_not_insert_safety_slot_when_it_is_the_last_occupied_slot()
 * @see ProbeInserterTest#visitFrame_should_insert_TOP_after_probe_variable_when_safety_slot_occupied_but_not_the_last()
 */
public class SafetySlotTest {

	@Test
	public void jvm_should_verify_original_class_without_errors()
			throws Exception {
		final byte[] original = createClass();

		new TargetLoader().add("Sample", original).newInstance();
	}

	@Test
	public void jvm_should_verify_instrumented_class_without_errors()
			throws Exception {
		final IRuntime runtime = new SystemPropertiesRuntime();
		runtime.startup(new RuntimeData());

		final byte[] original = createClass();
		final byte[] instrumented = new Instrumenter(runtime)
				.instrument(original, "Sample");

		new TargetLoader().add("Sample", instrumented).newInstance();
	}

	private static byte[] createClass() {
		final ClassWriter writer = new ClassWriter(0);
		writer.visit(bytecodeVersion(), Opcodes.ACC_PUBLIC, "Sample", null,
				"java/lang/Object", new String[0]);

		MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
				"()V", null, new String[0]);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);

		// Put a long value (2 slots) on position 0, overwriting 'this'
		mv.visitLdcInsn(Long.valueOf(42));
		mv.visitVarInsn(Opcodes.LSTORE, 0);

		mv.visitInsn(Opcodes.ICONST_0);
		final Label label1 = new Label();
		mv.visitJumpInsn(Opcodes.IFEQ, label1);
		mv.visitJumpInsn(Opcodes.GOTO, label1);
		mv.visitLabel(label1);
		mv.visitFrame(Opcodes.F_NEW, 1, new Object[] { Opcodes.LONG }, 0,
				new Object[] {});

		mv.visitLdcInsn(Integer.valueOf(13));
		mv.visitVarInsn(Opcodes.ISTORE, 2);
		mv.visitInsn(Opcodes.ICONST_0);
		final Label label2 = new Label();
		mv.visitJumpInsn(Opcodes.IFEQ, label2);
		mv.visitJumpInsn(Opcodes.GOTO, label2);
		mv.visitLabel(label2);
		mv.visitFrame(Opcodes.F_NEW, 2,
				new Object[] { Opcodes.LONG, Opcodes.INTEGER }, 0,
				new Object[] {});

		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();

		writer.visitEnd();

		return writer.toByteArray();
	}

	/**
	 * According to Java Virtual Machine Specification <a href=
	 * "https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.10.1">
	 * §4.10.1</a>:
	 *
	 * <blockquote>
	 * <p>
	 * A class file whose version number is 50.0 or above (§4.1) must be
	 * verified using the type checking rules given in this section.
	 * </p>
	 * <p>
	 * If, and only if, a class file's version number equals 50.0, then if the
	 * type checking fails, a Java Virtual Machine implementation may choose to
	 * attempt to perform verification by type inference (§4.10.2).
	 * </p>
	 * </blockquote>
	 *
	 * @return {@link Opcodes#V1_7} if supported by current JVM,
	 *         {@link Opcodes#V1_5} otherwise
	 */
	private static int bytecodeVersion() {
		return JavaVersion.current().isBefore("7") ? Opcodes.V1_5
				: Opcodes.V1_7;
	}

}
