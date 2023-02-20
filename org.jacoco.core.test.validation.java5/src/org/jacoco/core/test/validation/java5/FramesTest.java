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
package org.jacoco.core.test.validation.java5;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.java5.targets.BooleanExpressionsTarget;
import org.jacoco.core.test.validation.java5.targets.ClassInitializerTarget;
import org.jacoco.core.test.validation.java5.targets.ConstructorsTarget;
import org.jacoco.core.test.validation.java5.targets.ControlStructureBeforeSuperConstructorTarget;
import org.jacoco.core.test.validation.java5.targets.ControlStructuresTarget;
import org.jacoco.core.test.validation.java5.targets.ExceptionsTarget;
import org.jacoco.core.test.validation.java5.targets.ExplicitInitialFrameTarget;
import org.jacoco.core.test.validation.java5.targets.FieldInitializationInTwoConstructorsTarget;
import org.jacoco.core.test.validation.java5.targets.ImplicitFieldInitializationTarget;
import org.jacoco.core.test.validation.java5.targets.InterfaceClassInitializerTarget;
import org.jacoco.core.test.validation.java5.targets.StructuredLockingTarget;
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
			super(InstrSupport.ASM_API_VERSION, cv);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			final MethodVisitor mv = super.visitMethod(access, name, desc,
					signature, exceptions);
			return new MethodVisitor(InstrSupport.ASM_API_VERSION, mv) {
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
		ClassReader rc = InstrSupport.classReaderFor(source);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		// Adjust Version to 1.6 to enable frames:
		rc.accept(new ClassVisitor(InstrSupport.ASM_API_VERSION, cw) {

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
		new ClassReader(bytes).accept(
				new MaxStackEliminator(new TraceClassVisitor(writer)),
				ClassReader.EXPAND_FRAMES);
		return buffer.toString();
	}

	@Test
	public void boolean_expressions() throws IOException {
		testFrames(BooleanExpressionsTarget.class);
	}

	@Test
	public void class_initializer() throws IOException {
		testFrames(ClassInitializerTarget.class);
	}

	@Test
	public void constructors() throws IOException {
		testFrames(ConstructorsTarget.class);
	}

	@Test
	public void control_structures() throws IOException {
		testFrames(ControlStructuresTarget.class);
	}

	@Test
	public void control_structure_before_super_constructor()
			throws IOException {
		testFrames(ControlStructureBeforeSuperConstructorTarget.class);
	}

	@Test
	public void exceptions() throws IOException {
		testFrames(ExceptionsTarget.class);
	}

	@Test
	public void explicit_initial_frame() throws IOException {
		testFrames(ExplicitInitialFrameTarget.class);
	}

	@Test
	public void field_initialization_in_two_constructors() throws IOException {
		testFrames(FieldInitializationInTwoConstructorsTarget.class);
	}

	@Test
	public void implicit_field_initialization() throws IOException {
		testFrames(ImplicitFieldInitializationTarget.class);
	}

	@Test
	public void interface_class_initializer() throws IOException {
		testFrames(InterfaceClassInitializerTarget.class);
	}

	@Test
	public void structured_locking() throws IOException {
		testFrames(StructuredLockingTarget.class);
	}

}
