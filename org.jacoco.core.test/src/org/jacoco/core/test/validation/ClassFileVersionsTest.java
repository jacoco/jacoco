/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation;

import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_1;
import static org.objectweb.asm.Opcodes.V1_2;
import static org.objectweb.asm.Opcodes.V1_3;
import static org.objectweb.asm.Opcodes.V1_4;
import static org.objectweb.asm.Opcodes.V1_5;
import static org.objectweb.asm.Opcodes.V1_6;
import static org.objectweb.asm.Opcodes.V1_7;
import static org.objectweb.asm.Opcodes.V1_8;

import java.io.IOException;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * Test class inserted stackmap frames for different class file versions.
 */
public class ClassFileVersionsTest {

	@Test
	public void test_1_1() throws IOException {
		testVersion(V1_1, false);
	}

	@Test
	public void test_1_2() throws IOException {
		testVersion(V1_2, false);
	}

	@Test
	public void test_1_3() throws IOException {
		testVersion(V1_3, false);
	}

	@Test
	public void test_1_4() throws IOException {
		testVersion(V1_4, false);
	}

	@Test
	public void test_1_5() throws IOException {
		testVersion(V1_5, false);
	}

	@Test
	public void test_1_6() throws IOException {
		testVersion(V1_6, true);
	}

	@Test
	public void test_1_7() throws IOException {
		testVersion(V1_7, true);
	}

	@Test
	public void test_1_8() throws IOException {
		testVersion(V1_8, true);
	}

	private void testVersion(int version, boolean frames) throws IOException {
		final byte[] original = createClass(version);

		IRuntime runtime = new SystemPropertiesRuntime();
		Instrumenter instrumenter = new Instrumenter(runtime);
		byte[] instrumented = instrumenter.instrument(original, "TestTarget");

		assertFrames(instrumented, frames);
	}

	private void assertFrames(byte[] source, boolean expected) {
		final boolean[] hasFrames = new boolean[] { false };
		new ClassReader(source).accept(
				new ClassVisitor(JaCoCo.ASM_API_VERSION) {

					@Override
					public MethodVisitor visitMethod(int access, String name,
							String desc, String signature, String[] exceptions) {
						return new MethodVisitor(JaCoCo.ASM_API_VERSION) {

							@Override
							public void visitFrame(int type, int nLocal,
									Object[] local, int nStack, Object[] stack) {
								hasFrames[0] = true;
							}

						};
					}

				}, 0);
		assertEquals(Boolean.valueOf(expected), Boolean.valueOf(hasFrames[0]));
	}

	private byte[] createClass(int version) {

		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		cw.visit(version, ACC_PUBLIC + ACC_SUPER, "org/jacoco/test/Sample",
				null, "java/lang/Object", null);

		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V",
				false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();

		cw.visitEnd();

		return cw.toByteArray();
	}

}
