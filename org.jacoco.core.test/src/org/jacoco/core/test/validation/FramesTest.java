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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.targets.Target01;
import org.jacoco.core.test.validation.targets.Target02;
import org.jacoco.core.test.validation.targets.Target03;
import org.jacoco.core.test.validation.targets.Target04;
import org.jacoco.core.test.validation.targets.Target05;
import org.jacoco.core.test.validation.targets.Target06;
import org.jacoco.core.test.validation.targets.Target07;
import org.jacoco.core.test.validation.targets.Target08;
import org.jacoco.core.test.validation.targets.Target09;
import org.jacoco.core.test.validation.targets.Target10;
import org.jacoco.core.test.validation.targets.Target11;
import org.jacoco.core.test.validation.targets.Target12;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Tests whether stackmap frames are correctly adjusted.
 */
public class FramesTest {

	/**
	 * Stack sizes calculated for instrumented classes might be sometimes bigger
	 * than actually needed. This is an acceptable tradeoff in favor of keeping
	 * track of the actual stack sizes. For test assertions we need to replace
	 * max stack sizes with constant value.
	 */
	private static class MaxStackEliminator extends ClassVisitor {
		public MaxStackEliminator(ClassVisitor cv) {
			super(JaCoCo.ASM_API_VERSION, cv);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			final MethodVisitor mv = super.visitMethod(access, name, desc,
					signature, exceptions);
			return new MethodVisitor(JaCoCo.ASM_API_VERSION, mv) {
				@Override
				public void visitMaxs(int maxStack, int maxLocals) {
					super.visitMaxs(-1, maxLocals);
				}
			};
		}
	}

	private void testFrames(Class<?> target) throws IOException {
		testFrames(TargetLoader.getClassDataAsBytes(target));
	}

	private void testFrames(byte[] source) throws IOException {
		IRuntime runtime = new SystemPropertiesRuntime();
		Instrumenter instrumenter = new Instrumenter(runtime);
		source = calculateFrames(source);
		byte[] actual = instrumenter.instrument(source, "TestTarget");
		byte[] expected = calculateFrames(actual);

		assertEquals(dump(expected), dump(actual));
	}

	private byte[] calculateFrames(byte[] source) {
		ClassReader rc = new ClassReader(source);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		// Adjust Version to 1.6 to enable frames:
		rc.accept(new ClassVisitor(JaCoCo.ASM_API_VERSION, cw) {

			@Override
			public void visit(int version, int access, String name,
					String signature, String superName, String[] interfaces) {
				super.visit(Opcodes.V1_6, access, name, signature, superName,
						interfaces);
			}
		}, 0);
		return cw.toByteArray();
	}

	private String dump(byte[] bytes) {
		final StringWriter buffer = new StringWriter();
		final PrintWriter writer = new PrintWriter(buffer);
		new ClassReader(bytes).accept(new MaxStackEliminator(
				new TraceClassVisitor(writer)), ClassReader.EXPAND_FRAMES);
		return buffer.toString();
	}

	@Test
	public void testTarget01() throws IOException {
		testFrames(Target01.class);
	}

	@Test
	public void testTarget02() throws IOException {
		testFrames(Target02.class);
	}

	@Test
	public void testTarget03() throws IOException {
		testFrames(Target03.class);
	}

	@Test
	public void testTarget04() throws IOException {
		testFrames(Target04.class);
	}

	@Test
	public void testTarget05() throws IOException {
		testFrames(Target05.class);
	}

	@Test
	public void testTarget06() throws IOException {
		testFrames(Target06.class);
	}

	@Test
	public void testTarget07() throws IOException {
		testFrames(Target07.class);
	}

	@Test
	public void testTarget08() throws IOException {
		testFrames(Target08.class);
	}

	@Test
	public void testTarget09() throws IOException {
		testFrames(Target09.class);
	}

	@Test
	public void testTarget10() throws IOException {
		testFrames(Target10.class);
	}

	@Test
	public void testTarget11() throws IOException {
		testFrames(Target11.class);
	}

	@Test
	public void testTarget12() throws IOException {
		testFrames(Target12.class);
	}

}
