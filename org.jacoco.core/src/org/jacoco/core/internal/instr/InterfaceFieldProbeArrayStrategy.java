/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This strategy for Java 8 interfaces adds a static method requesting the probe
 * array from the runtime, a static field to hold the probe array and adds code
 * for its initialization into interface initialization method.
 */
class InterfaceFieldProbeArrayStrategy implements IProbeArrayStrategy {

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
	private final int probeCount;
	private final IExecutionDataAccessorGenerator accessorGenerator;

	private boolean seenClinit = false;

	InterfaceFieldProbeArrayStrategy(final String className, final long classId,
			final int probeCount,
			final IExecutionDataAccessorGenerator accessorGenerator) {
		this.className = className;
		this.classId = classId;
		this.probeCount = probeCount;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(final MethodVisitor mv, final boolean clinit,
			final int variable) {
		if (clinit) {
			final int maxStack = accessorGenerator.generateDataAccessor(classId,
					className, probeCount, mv);

			// Stack[0]: [Z

			mv.visitInsn(Opcodes.DUP);

			// Stack[1]: [Z
			// Stack[0]: [Z

			mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
					InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);

			// Stack[0]: [Z

			mv.visitVarInsn(Opcodes.ASTORE, variable);

			seenClinit = true;
			return Math.max(maxStack, 2);
		} else {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
					InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
					true);
			mv.visitVarInsn(Opcodes.ASTORE, variable);
			return 1;
		}
	}

	public void addMembers(final ClassVisitor cv, final int probeCount) {
		createDataField(cv);
		createInitMethod(cv, probeCount);
		if (!seenClinit) {
			createClinitMethod(cv, probeCount);
		}
	}

	private void createDataField(final ClassVisitor cv) {
		cv.visitField(InstrSupport.DATAFIELD_INTF_ACC,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC, null,
				null);
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
		final int size = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);

		// Stack[0]: [Z

		// Return the class' probe array:
		mv.visitFrame(Opcodes.F_NEW, 0, FRAME_LOCALS_EMPTY, 1,
				FRAME_STACK_ARRZ);
		mv.visitLabel(alreadyInitialized);
		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(Math.max(size, 2), 0); // Maximum local stack size is 2
		mv.visitEnd();
	}

	private void createClinitMethod(final ClassVisitor cv,
			final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.CLINIT_ACC,
				InstrSupport.CLINIT_NAME, InstrSupport.CLINIT_DESC, null, null);
		mv.visitCode();

		final int maxStack = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);

		// Stack[0]: [Z

		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);

		mv.visitInsn(Opcodes.RETURN);

		mv.visitMaxs(maxStack, 0);
		mv.visitEnd();
	}

}
