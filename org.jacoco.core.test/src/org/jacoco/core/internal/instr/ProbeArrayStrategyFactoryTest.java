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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

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
		final IProbeArrayStrategy strategy = test(Opcodes.V1_1, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass2() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_2, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass3() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_3, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass4() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_4, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass5() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_5, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass6() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_6, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testClass7() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_7, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testClass8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(true);

		strategy.storeInstance(cv.visitMethod(0, null, null, null, null), false,
				0);
	}

	@Test
	public void testInterface7() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_7,
				Opcodes.ACC_INTERFACE, true, false, true);
		assertEquals(LocalProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test
	public void testEmptyInterface7() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_7,
				Opcodes.ACC_INTERFACE, false, false, false);
		assertEquals(NoneProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyInterface7StoreInstance() {
		IProbeArrayStrategy strategy = test(Opcodes.V1_7, Opcodes.ACC_INTERFACE,
				false, false, false);
		strategy.storeInstance(null, false, 0);
	}

	@Test
	public void testInterface8() {
		cv.isInterface = true;
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, false, true, true);
		assertEquals(InterfaceFieldProbeArrayStrategy.class,
				strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_INTF_ACC);
		assertInitAndClinitMethods();

		strategy.storeInstance(cv.visitMethod(0, null, null, null, null), false,
				0);
	}

	@Test
	public void testEmptyInterface8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, false, false, false);
		assertEquals(NoneProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyInterface8StoreInstance() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, false, false, false);
		strategy.storeInstance(null, false, 0);
	}

	@Test
	public void testClinitAndAbstractMethodsInterface8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, true, false, true);
		assertEquals(LocalProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();

		strategy.storeInstance(cv.visitMethod(0, null, null, null, null), false,
				0);
	}

	@Test
	public void testClinitInterface8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, true, false, false);
		assertEquals(LocalProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test
	public void testClinitAndMethodsInterface8() {
		cv.isInterface = true;
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, true, true, true);
		assertEquals(InterfaceFieldProbeArrayStrategy.class,
				strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_INTF_ACC);
		assertInitAndClinitMethods();

		strategy.storeInstance(cv.visitMethod(0, "<clinit>", null, null, null),
				true, 0);
	}

	@Test
	public void test_java9_module() {
		final IProbeArrayStrategy strategy = createForModule(Opcodes.V9);
		assertEquals(NoneProbeArrayStrategy.class, strategy.getClass());
	}

	@Test
	public void test_java11_class() {
		final IProbeArrayStrategy strategy = test(Opcodes.V11, 0, true, true,
				true);

		assertEquals(CondyProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertCondyBootstrapMethod();
	}

	@Test
	public void test_java11_interface_with_clinit_and_methods() {
		final IProbeArrayStrategy strategy = test(Opcodes.V11,
				Opcodes.ACC_INTERFACE, true, true, true);

		assertEquals(CondyProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertCondyBootstrapMethod();
	}

	@Test
	public void test_java11_interface_with_clinit() {
		final IProbeArrayStrategy strategy = test(Opcodes.V11,
				Opcodes.ACC_INTERFACE, true, false, true);

		assertEquals(LocalProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test
	public void test_java11_interface_without_code() {
		final IProbeArrayStrategy strategy = test(Opcodes.V11,
				Opcodes.ACC_INTERFACE, false, false, true);

		assertEquals(NoneProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test
	public void test_java11_module() {
		final IProbeArrayStrategy strategy = createForModule(Opcodes.V11);
		assertEquals(NoneProbeArrayStrategy.class, strategy.getClass());
	}

	private IProbeArrayStrategy createForModule(int version) {
		final ClassWriter writer = new ClassWriter(0);
		writer.visit(version, Opcodes.ACC_MODULE, "module-info", null, null,
				null);
		writer.visitModule("module", 0, null).visitEnd();
		writer.visitEnd();
		return ProbeArrayStrategyFactory.createFor(0,
				new ClassReader(writer.toByteArray()), generator);
	}

	private IProbeArrayStrategy test(int version, int access, boolean clinit,
			boolean method, boolean abstractMethod) {
		final ClassWriter writer = new ClassWriter(0);
		writer.visit(version, access, "Foo", "java/lang/Object", null, null);
		if (clinit) {
			final MethodVisitor mv = writer.visitMethod(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V",
					null, null);
			mv.visitCode();
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		if (method) {
			final MethodVisitor mv = writer.visitMethod(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "doit", "()V",
					null, null);
			mv.visitCode();
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		if (abstractMethod) {
			final MethodVisitor mv = writer.visitMethod(Opcodes.ACC_ABSTRACT,
					"foo", "()V", null, null);
			mv.visitEnd();
		}
		writer.visitEnd();

		final IProbeArrayStrategy strategy = ProbeArrayStrategyFactory
				.createFor(0, new ClassReader(writer.toByteArray()), generator);

		strategy.addMembers(cv, 123);
		return strategy;
	}

	private static class AddedMethod {
		private final int access;
		private final String name;
		private final String desc;
		private boolean frames;

		AddedMethod(int access, String name, String desc) {
			this.access = access;
			this.name = name;
			this.desc = desc;
		}

		void assertInitMethod(String expectedDesc, boolean frames) {
			assertEquals(InstrSupport.INITMETHOD_NAME, name);
			assertEquals(expectedDesc, desc);
			assertEquals(InstrSupport.INITMETHOD_ACC, access);
			assertEquals(Boolean.valueOf(frames), Boolean.valueOf(frames));
		}

		void assertClinit() {
			assertEquals(InstrSupport.CLINIT_NAME, name);
			assertEquals(InstrSupport.CLINIT_DESC, desc);
			assertEquals(InstrSupport.CLINIT_ACC, access);
			assertEquals(Boolean.valueOf(false), Boolean.valueOf(frames));
		}
	}

	private static class ClassVisitorMock extends ClassVisitor {

		private boolean isInterface;

		private int fieldAccess;
		private String fieldName;
		private final List<AddedMethod> methods = new ArrayList<AddedMethod>();

		ClassVisitorMock() {
			super(InstrSupport.ASM_API_VERSION);
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
			final AddedMethod m = new AddedMethod(access, name, desc);
			methods.add(m);
			return new MethodVisitor(InstrSupport.ASM_API_VERSION) {
				@Override
				public void visitFrame(int type, int nLocal, Object[] local,
						int nStack, Object[] stack) {
					m.frames = true;
				}

				@Override
				public void visitFieldInsn(int opcode, String owner,
						String name, String desc) {
					assertEquals(InstrSupport.DATAFIELD_NAME, name);
					assertEquals(InstrSupport.DATAFIELD_DESC, desc);

					if (opcode == Opcodes.GETSTATIC) {
						assertEquals(InstrSupport.INITMETHOD_NAME,
								methods.get(methods.size() - 1).name);
					} else if (opcode == Opcodes.PUTSTATIC) {
						if (isInterface) {
							assertEquals(InstrSupport.CLINIT_NAME,
									methods.get(methods.size() - 1).name);
						} else {
							assertEquals(InstrSupport.INITMETHOD_NAME,
									methods.get(methods.size() - 1).name);
						}
					} else {
						fail();
					}
				}

				@Override
				public void visitMethodInsn(int opcode, String owner,
						String name, String desc, boolean itf) {
					if ("getProbes".equals(name)) {
						// method's owner is not interface:
						assertFalse(itf);
						return;
					}
					assertEquals(Boolean.valueOf(itf),
							Boolean.valueOf(isInterface));

					assertEquals(Opcodes.INVOKESTATIC, opcode);
					assertEquals("Foo", owner);
					assertEquals(InstrSupport.INITMETHOD_NAME, name);
					assertEquals(InstrSupport.INITMETHOD_DESC, desc);
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
		assertEquals(cv.methods.size(), 1);
		cv.methods.get(0).assertInitMethod(InstrSupport.INITMETHOD_DESC,
				frames);
	}

	void assertCondyBootstrapMethod() {
		assertEquals(cv.methods.size(), 1);
		cv.methods.get(0).assertInitMethod(CondyProbeArrayStrategy.B_DESC,
				false);
	}

	void assertInitAndClinitMethods() {
		assertEquals(2, cv.methods.size());
		cv.methods.get(0).assertInitMethod(InstrSupport.INITMETHOD_DESC, true);
		cv.methods.get(1).assertClinit();
	}

	void assertNoInitMethod() {
		assertEquals(0, cv.methods.size());
	}

}
