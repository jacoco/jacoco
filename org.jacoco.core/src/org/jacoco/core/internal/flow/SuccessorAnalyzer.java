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
package org.jacoco.core.internal.flow;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Method visitor that analyzes whether an instruction can be the successor of
 * the previous instruction. This is the case if it is the first instruction or
 * or if the predecessor instruction is not one of the following opcodes:
 * 
 * <ul>
 * <li>{@link Opcodes#GOTO}</li>
 * <li>{@link Opcodes#RETURN}</li>
 * <li>{@link Opcodes#IRETURN}</li>
 * <li>{@link Opcodes#LRETURN}</li>
 * <li>{@link Opcodes#FRETURN}</li>
 * <li>{@link Opcodes#DRETURN}</li>
 * <li>{@link Opcodes#ARETURN}</li>
 * <li>{@link Opcodes#ATHROW}</li>
 * <li>{@link Opcodes#LOOKUPSWITCH}</li>
 * <li>{@link Opcodes#TABLESWITCH}</li>
 * </ul>
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public abstract class SuccessorAnalyzer implements MethodVisitor {

	/**
	 * <code>true</code> if the current instruction is a potential successor of
	 * the previous instruction.
	 */
	protected boolean successor = true;

	public void visitInsn(final int opcode) {
		switch (opcode) {
		case Opcodes.RET:
			throw new AssertionError("Subroutines not supported.");
		case Opcodes.IRETURN:
		case Opcodes.LRETURN:
		case Opcodes.FRETURN:
		case Opcodes.DRETURN:
		case Opcodes.ARETURN:
		case Opcodes.RETURN:
		case Opcodes.ATHROW:
			successor = false;
			break;
		default:
			successor = true;
			break;
		}
	}

	public void visitIntInsn(final int opcode, final int operand) {
		successor = true;
	}

	public void visitVarInsn(final int opcode, final int var) {
		successor = true;
	}

	public void visitTypeInsn(final int opcode, final String type) {
		successor = true;
	}

	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		successor = true;
	}

	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		successor = true;
	}

	public void visitJumpInsn(final int opcode, final Label label) {
		if (opcode == Opcodes.JSR) {
			throw new AssertionError("Subroutines not supported.");
		}
		successor = opcode != Opcodes.GOTO;
	}

	public void visitLdcInsn(final Object cst) {
		successor = true;
	}

	public void visitIincInsn(final int var, final int increment) {
		successor = true;
	}

	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		successor = false;
	}

	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		successor = false;
	}

	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		successor = true;
	}

}
