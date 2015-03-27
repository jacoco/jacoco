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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Experimental generator for "companion classes" which hold the state for
 * instrumented classes to avoid adding member.
 */
public class CompanionClassStrategy implements IProbeArrayStrategy {

	private static final Method DEFINE_CLASS;

	static {
		try {
			DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass",
					String.class, byte[].class, Integer.TYPE, Integer.TYPE);
			DEFINE_CLASS.setAccessible(true);
		} catch (final SecurityException e) {
			throw new RuntimeException(e);
		} catch (final NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private final String className;
	private final String companionName;
	private final long classId;
	private final IExecutionDataAccessorGenerator accessorGenerator;
	private final ClassLoader classLoader;

	public CompanionClassStrategy(final String className, final long classId,
			final IExecutionDataAccessorGenerator accessorGenerator,
			final ClassLoader classLoader) {
		this.className = className;
		this.classLoader = classLoader;
		this.companionName = String.format("%s$jacoco%016x", className,
				Long.valueOf(classId));
		this.classId = classId;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(final MethodVisitor mv, final int variable) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, companionName,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 1;
	}

	public void addMembers(final ClassVisitor cv, final int probeCount) {
		// we reuse this to create and load the companion class
		loadCompanionClass(createCompanionClass(probeCount));
	}

	private byte[] createCompanionClass(final int probeCount) {
		final ClassWriter cw = new ClassWriter(0);

		cw.visit(Opcodes.V1_1, Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL,
				companionName, null, "java/lang/Object", null);

		cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_TRANSIENT
				| Opcodes.ACC_FINAL, InstrSupport.DATAFIELD_NAME,
				InstrSupport.DATAFIELD_DESC, null, null);

		final MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>",
				"()V", null, null);
		mv.visitCode();
		final int stackSize = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, companionName,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(stackSize, 0);
		mv.visitEnd();

		cw.visitEnd();
		return cw.toByteArray();
	}

	private void loadCompanionClass(final byte[] definition) {
		try {
			classLoader.loadClass(companionName);
			// companion does already exist:
			return;
		} catch (final ClassNotFoundException e) {
			// expected
		}
		try {
			DEFINE_CLASS.invoke(classLoader, companionName.replace('/', '.'),
					definition, Integer.valueOf(0),
					Integer.valueOf(definition.length));
		} catch (final IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (final InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		}
	}

}
