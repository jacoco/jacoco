/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.instr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * This method adapter instruments a method to record every block that gets
 * fully executed.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class MethodInstrumenter extends GeneratorAdapter implements
		IBlockMethodVisitor {

	private final String enclosingType;

	private int probeArray;

	/**
	 * Create a new instrumenter instance for the given method.
	 * 
	 * @param mv
	 *            next method visitor in the chain
	 * @param access
	 *            access flags for the method
	 * @param name
	 *            name of the method
	 * @param desc
	 *            description of the method
	 * @param enclosingType
	 *            type enclosing this method
	 */
	public MethodInstrumenter(final MethodVisitor mv, final int access,
			final String name, final String desc, final String enclosingType) {
		super(mv, access, name, desc);
		this.enclosingType = enclosingType;
	}

	@Override
	public void visitCode() {
		super.visitCode();
		// At the very beginning of the method we load the boolean[] array into
		// a local variable that stores the probes for this class.
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, enclosingType,
				GeneratorConstants.INITMETHOD_NAME,
				GeneratorConstants.INITMETHOD_DESC);

		// Stack[0]: [Z

		probeArray = newLocal(GeneratorConstants.PROBEDATA_TYPE);
		mv.visitVarInsn(Opcodes.ASTORE, probeArray);
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		// Max stack size of the probe code is 3
		super.visitMaxs(maxStack + 3, maxLocals + 1);
	}

	// === IBlockMethodVisitor ===

	public void visitBlockEndBeforeJump(final int id) {
		// At the end of every block we set the corresponding position in the
		// boolean[] array to true.

		mv.visitVarInsn(Opcodes.ALOAD, probeArray);

		// Stack[0]: [Z

		push(id);

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
