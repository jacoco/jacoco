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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @see org.jacoco.core.internal.flow.LabelFlowAnalyzer#visitLineNumber(int,
 *      Label)
 * @see org.jacoco.core.internal.analysis.MethodAnalyzerTest#zero_line_number_should_create_1_probe()
 */
public class ZeroLineNumberTest {

	@Test
	public void zero_line_numbers_should_be_preserved_during_instrumentation_and_should_not_cause_insertion_of_additional_probes()
			throws Exception {
		final IRuntime runtime = new SystemPropertiesRuntime();
		final RuntimeData data = new RuntimeData();
		runtime.startup(data);

		final byte[] original = createClass();
		final byte[] instrumented = new Instrumenter(runtime)
				.instrument(original, "Sample");

		final Class<?> cls = new TargetLoader().add("Sample", instrumented);
		try {
			cls.newInstance();
			fail("Exception expected");
		} catch (final Exception e) {
			assertEquals(0, e.getStackTrace()[1].getLineNumber());
		}

		data.getExecutionData(CRC64.classId(original), "Sample", 2);
	}

	private static byte[] createClass() {
		final ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cv.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Sample", null,
				"java/lang/Object", null);
		cv.visitSource("Sample.java", null);

		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V",
				null, null);
		mv.visitCode();
		final Label label1 = new Label();
		mv.visitLabel(label1);
		mv.visitLineNumber(1, label1);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		final Label label2 = new Label();
		mv.visitLabel(label2);
		mv.visitLineNumber(0, label2);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Sample", "throw", "()V",
				false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		mv = cv.visitMethod(Opcodes.ACC_STATIC, "throw", "()V", null, null);
		mv.visitCode();
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
		mv.visitInsn(Opcodes.DUP);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException",
				"<init>", "()V", false);
		mv.visitInsn(Opcodes.ATHROW);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		cv.visitEnd();
		return cv.toByteArray();
	}

}
