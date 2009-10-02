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
import org.objectweb.asm.Type;
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

	private final int methodId;

	private final Type enclosingType;

	private int blockArray;

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
	 * @param methodId
	 *            unique id of the method within its enclosing type
	 * @param enclosingType
	 *            type enclosing this method
	 */
	public MethodInstrumenter(final MethodVisitor mv, final int access,
			final String name, final String desc, final int methodId,
			final Type enclosingType) {
		super(mv, access, name, desc);
		this.methodId = methodId;
		this.enclosingType = enclosingType;
	}

	@Override
	public void visitCode() {
		super.visitCode();
		// At the very beginning of the method we load the boolean[] array into
		// a local variable that stores the block coverage of this method.

		push(methodId);

		// Stack[0]: I

		invokeStatic(enclosingType, GeneratorConstants.INIT_METHOD);

		// Stack[0]: [Z

		blockArray = newLocal(GeneratorConstants.BLOCK_ARR);
		storeLocal(blockArray);
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

		loadLocal(blockArray);

		// Stack[0]: [Z

		push(id);

		// Stack[1]: I
		// Stack[0]: [Z

		push(1);

		// Stack[2]: I
		// Stack[1]: I
		// Stack[0]: [Z

		visitInsn(Opcodes.BASTORE);
	}

	public void visitBlockEnd(final int id) {
		// nothing to do here
	}

}
