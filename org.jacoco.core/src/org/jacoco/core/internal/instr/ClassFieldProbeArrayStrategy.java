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

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * The strategy for regular classes adds a static field to hold the probe array
 * and a static initialization method requesting the probe array from the
 * runtime.
 */
class ClassFieldProbeArrayStrategy implements IProbeArrayStrategy {

	/**
	 * Frame stack with a single boolean array.
	 */
	private static final Object[] FRAME_STACK_ARRZ = new Object[] {
			InstrSupport.DATAFIELD_DESC };

	/**
	 * Empty frame locals.
	 */
	private static final Object[] FRAME_LOCALS_EMPTY = new Object[0];

	private final String className;
	private final long classId;
	private final boolean withFrames;
	private final IExecutionDataAccessorGenerator accessorGenerator;

	ClassFieldProbeArrayStrategy(final String className, final long classId,
			final boolean withFrames,
			final IExecutionDataAccessorGenerator accessorGenerator) {
		this.className = className;
		this.classId = classId;
		this.withFrames = withFrames;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(final MethodVisitor mv, final boolean clinit,
			final int variable) {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				false);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 1;
	}

	public void addMembers(final ClassVisitor cv, final int probeCount) {
		createDataField(cv);
		createInitMethod(cv, probeCount);
	}

	private void createDataField(final ClassVisitor cv) {
		cv.visitField(InstrSupport.DATAFIELD_ACC, InstrSupport.DATAFIELD_NAME,
				InstrSupport.DATAFIELD_DESC, null, null);
	}

	private void createInitMethod(final ClassVisitor cv, final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				null, null);
		mv.visitCode();

		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Z
		// Stack[0]: [Z

		// Skip initialization when we already have a data array:
		final Label alreadyInitialized = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);

		// Stack[0]: [Z

		mv.visitInsn(Opcodes.POP);
		final int size = genInitializeDataField(mv, probeCount);

		// Stack[0]: [Z

		// Return the class' probe array:
		if (withFrames) {
			mv.visitFrame(Opcodes.F_NEW, 0, FRAME_LOCALS_EMPTY, 1,
					FRAME_STACK_ARRZ);
		}
		mv.visitLabel(alreadyInitialized);
		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(Math.max(size, 2), 0); // Maximum local stack size is 2
		mv.visitEnd();
	}

	/**
	 * Generates the byte code to initialize the static coverage data field
	 * within this class.
	 *
	 * The code will push the [Z data array on the operand stack.
	 *
	 * @param mv
	 *            generator to emit code to
	 */
	private int genInitializeDataField(final MethodVisitor mv,
			final int probeCount) {
		final int size = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);

		// Stack[0]: [Z

		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Z
		// Stack[0]: [Z

		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);

		// Stack[0]: [Z

		return Math.max(size, 2); // Maximum local stack size is 2
	}

}
