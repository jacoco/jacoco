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
 * TODO The instrumented method may need a increased stack but this adapter will
 * *not* adjust the max stack size value. Therefore the ClassWriter needs to be
 * invoked with COMPUTE_MAXS. For performance optimization this adapter should
 * adjust the directly calculate the max stack size and report it properly.
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
	public MethodInstrumenter(MethodVisitor mv, int access, String name,
			String desc, int methodId, Type enclosingType) {
		super(mv, access, name, desc);
		this.methodId = methodId;
		this.enclosingType = enclosingType;
	}

	@Override
	public void visitCode() {
		super.visitCode();
		// At the very beginning of the method we load the boolean[] array into
		// a local variable that stores the block coverage of this method.

		push(methodId); // ................................... Stack: I
		invokeStatic(enclosingType, GeneratorConstants.INIT_METHOD); // ... Stack: [Z
		blockArray = newLocal(GeneratorConstants.BLOCK_ARR);
		storeLocal(blockArray); // ........................... Stack: <empty>
	}

	// === IBlockMethodVisitor ===

	public void visitBlockEndBeforeJump(int id) {
		// At the end of every block we set the corresponding position in the
		// boolean[] array to true.

		loadLocal(blockArray); // ............................ Stack: [Z
		push(id); // ......................................... Stack: [Z, I
		push(1); // .......................................... Stack: [Z, I, I
		visitInsn(Opcodes.BASTORE); // ....................... Stack: <empty>
	}

	public void visitBlockEnd(int id) {
		// nothing to do here
	}

}
