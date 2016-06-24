/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * This strategy adds two methods - static initialization method with
 * <code>invokedynamic</code> instruction and bootstrap method, which requests
 * the probe array from the runtime and produces <code>ConstantCallSite</code>
 * bound to <code>MethodHandle</code> returning this array. So that this
 * strategy avoids addition of field, but suitable only for Java 7 classes
 * and Java 8 interfaces.
 */
class InvokeDynamicProbeArrayStrategy implements IProbeArrayStrategy {

	private static final String BOOTSTRAP_METHOD_NAME = "$jacocoBootstrap";
	private static final String BOOTSTRAP_METHOD_DESC = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";

	private final String className;
	private final long classId;
	private final IExecutionDataAccessorGenerator accessorGenerator;

	public InvokeDynamicProbeArrayStrategy(final String className,
			final long classId,
			final IExecutionDataAccessorGenerator accessorGenerator) {
		this.className = className;
		this.classId = classId;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(final MethodVisitor mv, final int variable) {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				false);
		return 1;
	}

	public void addMembers(ClassVisitor cv, int probeCount) {
		createBootstrapMethod(cv, probeCount);
		createInitMethod(cv);
	}

	private void createInitMethod(final ClassVisitor cv) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				null, null);
		mv.visitCode();
		mv.visitInvokeDynamicInsn("$jacocoGetProbeArray", "()[Z",
				new Handle(Opcodes.H_INVOKESTATIC, className,
						BOOTSTRAP_METHOD_NAME, BOOTSTRAP_METHOD_DESC, false));
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(1, 0);
		mv.visitEnd();
	}

	private void createBootstrapMethod(final ClassVisitor cv,
			final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				BOOTSTRAP_METHOD_NAME, BOOTSTRAP_METHOD_DESC, null, null);
		mv.visitCode();

		mv.visitTypeInsn(Opcodes.NEW, "java/lang/invoke/ConstantCallSite");
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn(Type.getType(boolean[].class));
		final int size = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);
		// Stack[3]: [Z
		// Stack[2]: Ljava/lang/Class
		// Stack[1]: Ljava/lang/invoke/ConstantCallSite
		// Stack[0]: Ljava/lang/invoke/ConstantCallSite

		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"java/lang/invoke/MethodHandles", "constant",
				"(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;",
				false);
		// Stack[2]: Ljava/lang/invoke/MethodHandle
		// Stack[1]: Ljava/lang/invoke/ConstantCallSite
		// Stack[0]: Ljava/lang/invoke/ConstantCallSite

		mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/invoke/ConstantCallSite", "<init>",
				"(Ljava/lang/invoke/MethodHandle;)V", false);
		// Stack[0]: Ljava/lang/invoke/ConstantCallSite

		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(3 + size, 3);
		mv.visitEnd();
	}

}
