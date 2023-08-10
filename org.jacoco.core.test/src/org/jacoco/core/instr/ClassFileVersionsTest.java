/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.F_NEW;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V10;
import static org.objectweb.asm.Opcodes.V11;
import static org.objectweb.asm.Opcodes.V12;
import static org.objectweb.asm.Opcodes.V13;
import static org.objectweb.asm.Opcodes.V14;
import static org.objectweb.asm.Opcodes.V15;
import static org.objectweb.asm.Opcodes.V16;
import static org.objectweb.asm.Opcodes.V1_1;
import static org.objectweb.asm.Opcodes.V1_2;
import static org.objectweb.asm.Opcodes.V1_3;
import static org.objectweb.asm.Opcodes.V1_4;
import static org.objectweb.asm.Opcodes.V1_5;
import static org.objectweb.asm.Opcodes.V1_6;
import static org.objectweb.asm.Opcodes.V1_7;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Opcodes.V9;

import java.io.IOException;

import org.jacoco.core.internal.instr.CondyProbeArrayStrategy;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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

	@Test
	public void test_9() throws IOException {
		testVersion(V9, true);
	}

	@Test
	public void test_10() throws IOException {
		testVersion(V10, true);
	}

	@Test
	public void test_11() throws IOException {
		testVersion(V11, true);
	}

	@Test
	public void test_12() throws IOException {
		testVersion(V12, true);
	}

	@Test
	public void test_13() throws IOException {
		testVersion(V13, true);
	}

	@Test
	public void test_14() throws IOException {
		testVersion(V14, true);
	}

	@Test
	public void test_15() throws IOException {
		testVersion(V15, true);
	}

	@Test
	public void test_16() throws IOException {
		testVersion(V16, true);
	}

	private void testVersion(int version, boolean frames) throws IOException {
		final byte[] original = createClass(version, frames);

		IRuntime runtime = new SystemPropertiesRuntime();
		Instrumenter instrumenter = new Instrumenter(runtime);
		byte[] instrumented = instrumenter.instrument(original, "TestTarget");

		assertFrames(instrumented, frames);
	}

	private void assertFrames(byte[] source, final boolean expected) {
		InstrSupport.classReaderFor(source)
				.accept(new ClassVisitor(InstrSupport.ASM_API_VERSION) {

					@Override
					public MethodVisitor visitMethod(int access, String name,
							final String desc, String signature,
							String[] exceptions) {
						return new MethodVisitor(InstrSupport.ASM_API_VERSION) {
							boolean frames = false;

							@Override
							public void visitFrame(int type, int nLocal,
									Object[] local, int nStack,
									Object[] stack) {
								frames = true;
							}

							@Override
							public void visitEnd() {
								if (CondyProbeArrayStrategy.B_DESC
										.equals(desc)) {
									assertFalse(
											"CondyProbeArrayStrategy does not need frames",
											frames);
								} else {
									assertEquals(Boolean.valueOf(expected),
											Boolean.valueOf(frames));
								}
							}
						};
					}
				}, 0);
	}

	/**
	 * Creates a class that requires a frame before the return statement. Also
	 * for this class instrumentation should insert another frame.
	 *
	 * <code><pre>
	 * public class Sample {
	 *   public Sample(boolean b){
	 *     if(b){
	 *       toString();
	 *     }
	 *     return;
	 *   }
	 * }
	 * </pre></code>
	 */
	private byte[] createClass(int version, boolean frames) {

		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		cw.visit(version, ACC_PUBLIC + ACC_SUPER, "org/jacoco/test/Sample",
				null, "java/lang/Object", null);

		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Z)V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V",
				false);
		mv.visitVarInsn(ILOAD, 1);
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString",
				"()Ljava/lang/String;", false);
		mv.visitInsn(POP);
		mv.visitLabel(l1);
		if (frames) {
			mv.visitFrame(F_NEW, 2,
					new Object[] { "org/jacoco/test/Sample", Opcodes.INTEGER },
					0, new Object[] {});
		}
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 2);
		mv.visitEnd();

		cw.visitEnd();

		return cw.toByteArray();
	}

}
