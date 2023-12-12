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
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
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

	private RuntimeData data;
	private TestStorage storage;

	@Before
	public void setup() {
		data = new RuntimeData();
		storage = new TestStorage();
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

		assertEquals(3, ((boolean[]) args[0]).length);

		data.collect(storage, storage, false);
		boolean[] data = (boolean[]) args[0];
		assertEquals(3, data.length, 0.0);
		assertFalse(data[0]);
		assertFalse(data[1]);
		assertFalse(data[2]);
		assertSame(storage.getData(123).getProbes(), data);
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
		boolean[] probes = data.getExecutionData(Long.valueOf(123), "Foo", 1)
				.getProbes();
		probes[0] = true;

		data.collect(storage, storage, true);

		assertFalse(probes[0]);
		assertEquals("testsession", storage.getSessionInfo().getId());
	}

	@Test
	public void testCollectWithoutReset() {
		data.setSessionId("testsession");
		boolean[] probes = data.getExecutionData(Long.valueOf(123), "Foo", 1)
				.getProbes();
		probes[0] = true;

		data.collect(storage, storage, false);

		assertTrue(probes[0]);
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
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
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
		Callable<?> callable = (Callable<?>) loader
				.add("Sample", writer.toByteArray()).newInstance();
		final Object[] args = (Object[]) callable.call();
		assertEquals(3, args.length, 0.0);
		assertEquals(Long.valueOf(1000), args[0]);
		assertEquals("Sample", args[1]);
		assertEquals(Integer.valueOf(15), args[2]);
	}

	@Test
	public void testGenerateAccessCall() throws Exception {
		final boolean[] probes = data
				.getExecutionData(Long.valueOf(1234), "Sample", 5).getProbes();

		final ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Sample", null,
				"java/lang/Object",
				new String[] { Type.getInternalName(Callable.class) });

		// Constructor
		MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
				"(Ljava/lang/Object;)V", null, new String[0]);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
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

		writer.visitField(Opcodes.ACC_PRIVATE, "access", "Ljava/lang/Object;",
				null, null);

		writer.visitEnd();
		final TargetLoader loader = new TargetLoader();
		Callable<?> callable = (Callable<?>) loader
				.add("Sample", writer.toByteArray())
				.getConstructor(Object.class).newInstance(data);
		assertSame(probes, callable.call());
	}

}
