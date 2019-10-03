/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.instr;

import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodParameterOverwriteTest {

	@Test
	public void run_original()
			throws IllegalAccessException, InstantiationException {
		final byte[] original = generateMethod();

		new TargetLoader().add("Sample", original).newInstance();
	}

	@Test
	public void run_instrumented() throws Exception {
		final IRuntime runtime = new SystemPropertiesRuntime();
		runtime.startup(new RuntimeData());

		final byte[] original = generateMethod();
		final byte[] instrumented = new Instrumenter(runtime)
				.instrument(original, "Sample");

		new TargetLoader().add("Sample", instrumented).newInstance();
	}

	private static byte[] generateMethod() {
		final ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Sample", null,
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

		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(2, 2);
		mv.visitEnd();

		writer.visitEnd();

		return writer.toByteArray();
	}

}
