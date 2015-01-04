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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link ProbeArrayStrategyFactory} and the
 * {@link IProbeArrayStrategy} implementations. The verifies the behaviour of
 * the returned {@link IProbeArrayStrategy} instances for different classes.
 */
public class ProbeArrayStrategyFactoryTest {

	private IExecutionDataAccessorGenerator generator;
	private ClassVisitorMock cv;

	@Before
	public void setup() {
		generator = new OfflineInstrumentationAccessGenerator();
		cv = new ClassVisitorMock();
	}

	@Test
	public void testClass1() {
		test(Opcodes.V1_1, 0, false, true);
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass2() {
		test(Opcodes.V1_2, 0, false, true);
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass3() {
		test(Opcodes.V1_3, 0, false, true);
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass4() {
		test(Opcodes.V1_4, 0, false, true);
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass5() {
		test(Opcodes.V1_5, 0, false, true);
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass6() {
		test(Opcodes.V1_6, 0, false, true);
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testClass7() {
		test(Opcodes.V1_7, 0, false, true);
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testClass8() {
		test(Opcodes.V1_8, 0, false, true);
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testInterface7() {
		test(Opcodes.V1_7, Opcodes.ACC_INTERFACE, true, false);
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test
	public void testEmptyInterface7() {
		test(Opcodes.V1_7, Opcodes.ACC_INTERFACE, false, false);
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyInterface7StoreInstance() {
		IProbeArrayStrategy strategy = test(Opcodes.V1_7,
				Opcodes.ACC_INTERFACE, false, false);
		strategy.storeInstance(null, 0);
	}

	@Test
	public void testInterface8() {
		test(Opcodes.V1_8, Opcodes.ACC_INTERFACE, false, true);
		assertDataField(InstrSupport.DATAFIELD_INTF_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testEmptyInterface8() {
		test(Opcodes.V1_8, Opcodes.ACC_INTERFACE, false, false);
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test
	public void testClinitInterface8() {
		test(Opcodes.V1_8, Opcodes.ACC_INTERFACE, true, false);
		assertNoDataField();
		assertNoInitMethod();
	}

	private IProbeArrayStrategy test(int version, int access, boolean clinit,
			boolean method) {
		ClassWriter writer = new ClassWriter(0);
		writer.visit(version, access, "Foo", "java/lang/Object", null, null);
		if (clinit) {
			MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC
					| Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		if (method) {
			MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC
					| Opcodes.ACC_STATIC, "doit", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		writer.visitEnd();

		final IProbeArrayStrategy strategy = ProbeArrayStrategyFactory
				.createFor(new ClassReader(writer.toByteArray()), generator);

		strategy.addMembers(cv, 123);
		return strategy;
	}

	private static class ClassVisitorMock extends ClassVisitor {

		private int fieldAccess;
		private String fieldName;

		private int methodAccess;
		private String methodName;

		private boolean frames;

		ClassVisitorMock() {
			super(Opcodes.ASM5);
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc,
				String signature, Object value) {
			assertNull(fieldName);
			fieldAccess = access;
			fieldName = name;
			return null;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			assertNull(methodName);
			methodAccess = access;
			methodName = name;
			return new MethodVisitor(Opcodes.ASM5) {
				@Override
				public void visitFrame(int type, int nLocal, Object[] local,
						int nStack, Object[] stack) {
					frames = true;
				}
			};
		}
	}

	void assertDataField(int access) {
		assertEquals(InstrSupport.DATAFIELD_NAME, cv.fieldName);
		assertEquals(access, cv.fieldAccess);
	}

	void assertNoDataField() {
		assertNull(cv.fieldName);
	}

	void assertInitMethod(boolean frames) {
		assertEquals(InstrSupport.INITMETHOD_NAME, cv.methodName);
		assertEquals(InstrSupport.INITMETHOD_ACC, cv.methodAccess);
		assertEquals(Boolean.valueOf(frames), Boolean.valueOf(cv.frames));
	}

	void assertNoInitMethod() {
		assertNull(cv.methodName);
	}

}
