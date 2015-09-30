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
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.instr.IProbeArray;
import org.jacoco.core.internal.instr.ProbeArrayService;
import org.jacoco.core.internal.instr.ProbeDoubleIntArray;
import org.jacoco.core.test.TargetLoader;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Unit tests for {@link RuntimeData}.
 * 
 */
public class RuntimeDataTest {
	public static class RuntimeDataExistsTest extends
			RuntimeDataTestBase<boolean[]> {

		@BeforeClass
		public static void setupProbeMode() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.exists);
		}

		@AfterClass
		public static void cleanupProbeMode() {
			ProbeArrayService.reset();
		}
	}

	public static class RuntimeDataCountTest extends
			RuntimeDataTestBase<AtomicIntegerArray> {

		@BeforeClass
		public static void setupProbeMode() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.count);
		}

		@AfterClass
		public static void cleanupProbeMode() {
			ProbeArrayService.reset();
		}
	}

	public static class RuntimeDataParallelTest extends
			RuntimeDataTestBase<ProbeDoubleIntArray> {

		@BeforeClass
		public static void setupProbeMode() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.parallelcount);
		}

		@AfterClass
		public static void cleanupProbeMode() {
			ProbeArrayService.reset();
		}
	}

	public static abstract class RuntimeDataTestBase<T> {

		private RuntimeData data;
		private TestStorage<T> storage;

		@Before
		public void setup() {
			data = new RuntimeData();
			storage = new TestStorage<T>();
		}

		@Test
		public void testGetSetSessionId() {
			assertNotNull(data.getSessionId());
			data.setSessionId("test-id");
			assertEquals("test-id", data.getSessionId());
		}

		@Test
		public void testGetProbes() {
			Object[] args = new Object[] { Long.valueOf(123), "Foo",
					Integer.valueOf(3) };
			data.equals(args);

			data.collect(storage, storage, false);
			IProbeArray<?> data = ProbeArrayService.newProbeArray(args[0]);
			assertEquals(3, data.length());
			assertFalse(data.isProbeCovered(0));
			assertFalse(data.isProbeCovered(1));
			assertFalse(data.isProbeCovered(2));
			assertSame(
					((IProbeArray<?>) storage.getData(123).getProbes())
							.getProbesObject(),
					data.getProbesObject());
			assertEquals("Foo", storage.getData(123).getName());
		}

		@Test
		public void testCollectEmpty() {
			data.collect(storage, storage, false);
			storage.assertSize(0);
		}

		@Test
		public void testCollectWithReset() {
			data.setSessionId("testsession");
			IProbeArray<?> probes = (IProbeArray<?>) data.getExecutionData(
					Long.valueOf(123), "Foo", 1).getProbes();
			probes.increment(0);

			data.collect(storage, storage, true);

			assertFalse(probes.isProbeCovered(0));
			assertEquals("testsession", storage.getSessionInfo().getId());
		}

		@Test
		public void testCollectWithoutReset() {
			data.setSessionId("testsession");
			IProbeArray<?> probes = (IProbeArray<?>) data.getExecutionData(
					Long.valueOf(123), "Foo", 1).getProbes();
			probes.increment(0);

			data.collect(storage, storage, false);

			assertTrue(probes.isProbeCovered(0));
			assertEquals("testsession", storage.getSessionInfo().getId());
		}

		@Test
		public void testEquals() {
			assertTrue(data.equals(data));
		}

		@Test
		public void testHashCode() {
			assertEquals(System.identityHashCode(data), data.hashCode());
		}

		@Test
		public void testGenerateArgumentArray() throws Exception {
			final ClassWriter writer = new ClassWriter(0);
			writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Sample", null,
					"java/lang/Object",
					new String[] { Type.getInternalName(Callable.class) });

			// Constructor
			MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
					"()V", null, new String[0]);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
					"<init>", "()V", false);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();

			// call()
			mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "call",
					"()Ljava/lang/Object;", null, new String[0]);
			mv.visitCode();
			RuntimeData.generateArgumentArray(1000, "Sample", 15, mv);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(5, 1);
			mv.visitEnd();

			writer.visitEnd();
			final TargetLoader loader = new TargetLoader();
			Callable<?> callable = (Callable<?>) loader.add("Sample",
					writer.toByteArray()).newInstance();
			final Object[] args = (Object[]) callable.call();
			assertEquals(3, args.length, 0.0);
			assertEquals(Long.valueOf(1000), args[0]);
			assertEquals("Sample", args[1]);
			assertEquals(Integer.valueOf(15), args[2]);
		}

		@Test
		public void testGenerateAccessCall() throws Exception {
			final IProbeArray<?> probes = (IProbeArray<?>) data
					.getExecutionData(Long.valueOf(1234), "Sample", 5)
					.getProbes();

			final ClassWriter writer = new ClassWriter(0);
			writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Sample", null,
					"java/lang/Object",
					new String[] { Type.getInternalName(Callable.class) });

			// Constructor
			MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
					"(Ljava/lang/Object;)V", null, new String[0]);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
					"<init>", "()V", false);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitFieldInsn(Opcodes.PUTFIELD, "Sample", "access",
					"Ljava/lang/Object;");
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();

			// call()
			mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "call",
					"()Ljava/lang/Object;", null, new String[0]);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, "Sample", "access",
					"Ljava/lang/Object;");
			RuntimeData.generateAccessCall(1234, "Sample", 5, mv);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(6, 1);
			mv.visitEnd();

			writer.visitField(Opcodes.ACC_PRIVATE, "access",
					"Ljava/lang/Object;", null, null);

			writer.visitEnd();
			final TargetLoader loader = new TargetLoader();
			Callable<?> callable = (Callable<?>) loader
					.add("Sample", writer.toByteArray())
					.getConstructor(Object.class).newInstance(data);
			assertEquals(probes.getProbesObject(), callable.call());
		}
	}
}
