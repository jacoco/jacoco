/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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
import static org.junit.Assert.assertSame;

import java.util.concurrent.Callable;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Unit tests for {@link ExecutionDataAccess}.
 */
public class ExecutionDataAccessTest {

	private ExecutionDataStore store;

	private Object access;

	@Before
	public void setup() {
		store = new ExecutionDataStore();
		access = new ExecutionDataAccess(store);
	}

	@Test
	public void testGetExecutionData1() {
		Object[] args = new Object[] { Long.valueOf(123), "Foo",
				Integer.valueOf(3) };
		access.equals(args);
		boolean[] data = (boolean[]) args[0];
		assertEquals(3, data.length, 0.0);
		assertFalse(data[0]);
		assertFalse(data[1]);
		assertFalse(data[2]);
		assertSame(store.get(123).getData(), data);
		assertEquals("Foo", store.get(123).getName());
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
				"()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();

		// call()
		mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "call",
				"()Ljava/lang/Object;", null, new String[0]);
		mv.visitCode();
		ExecutionDataAccess.generateArgumentArray(1000, "Sample", 15, mv);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(5, 1);
		mv.visitEnd();

		writer.visitEnd();
		final TargetLoader loader = new TargetLoader("Sample",
				writer.toByteArray());
		Callable<?> callable = (Callable<?>) loader.newTargetInstance();
		final Object[] args = (Object[]) callable.call();
		assertEquals(3, args.length, 0.0);
		assertEquals(Long.valueOf(1000), args[0]);
		assertEquals("Sample", args[1]);
		assertEquals(Integer.valueOf(15), args[2]);
	}

	@Test
	public void testGenerateAccessCall() throws Exception {
		final boolean[] data = store.get(Long.valueOf(1234), "Sample", 5)
				.getData();

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
				"()V");
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
		ExecutionDataAccess.generateAccessCall(1234, "Sample", 5, mv);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(6, 1);
		mv.visitEnd();

		writer.visitField(Opcodes.ACC_PRIVATE, "access", "Ljava/lang/Object;",
				null, null);

		writer.visitEnd();
		final TargetLoader loader = new TargetLoader("Sample",
				writer.toByteArray());
		Callable<?> callable = (Callable<?>) loader.getTargetClass()
				.getConstructor(Object.class).newInstance(access);
		assertSame(data, callable.call());
	}

}
