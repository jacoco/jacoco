/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.instr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This method adapter instruments a method to record every block that gets
 * fully executed.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
class MethodInstrumenter extends ProbeVariableInserter implements
		IBlockMethodVisitor {

	private final IProbeArrayStrategy probeArrayStrategy;

	private int accessorStackSize;

	/**
	 * Create a new instrumenter instance for the given method.
	 * 
	 * @param mv
	 *            next method visitor in the chain
	 * @param access
	 *            access flags for the method
	 * @param desc
	 *            description of the method
	 * @param probeArrayStrategy
	 *            strategy to get access to the probe array
	 */
	public MethodInstrumenter(final MethodVisitor mv, final int access,
			final String desc, final IProbeArrayStrategy probeArrayStrategy) {
		super(access, desc, mv);
		this.probeArrayStrategy = probeArrayStrategy;
	}

	@Override
	public void visitCode() {
		super.visitCode();
		// At the very beginning of the method we load the boolean[] array into
		// a local variable that stores the probes for this class.
		accessorStackSize = probeArrayStrategy.pushInstance(mv);

		// Stack[0]: [Z

		mv.visitVarInsn(Opcodes.ASTORE, variable);
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		// Max stack size of the probe code is 3 which can add to the
		// original stack size depending on the probe locations. The accessor
		// stack size is an absolute maximum, as the accessor code is inserted
		// at the very beginning of each method when the stack size is empty.
		final int increasedStack = Math.max(maxStack + 3, accessorStackSize);
		super.visitMaxs(increasedStack, maxLocals);
	}

	// === IBlockMethodVisitor ===

	public void visitBlockEndBeforeJump(final int id) {
		// At the end of every block we set the corresponding position in the
		// boolean[] array to true.

		mv.visitVarInsn(Opcodes.ALOAD, variable);

		// Stack[0]: [Z

		InstrSupport.push(mv, id);

		// Stack[1]: I
		// Stack[0]: [Z

		mv.visitInsn(Opcodes.ICONST_1);

		// Stack[2]: I
		// Stack[1]: I
		// Stack[0]: [Z

		visitInsn(Opcodes.BASTORE);
	}

	public void visitBlockEnd(final int id) {
		// nothing to do here
	}

}
