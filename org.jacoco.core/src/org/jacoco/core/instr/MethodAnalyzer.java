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

import org.jacoco.core.data.IMethodStructureOutput;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * This {@link IBlockMethodVisitor} analyzes the block structure of a method and
 * reports it to a {@link IMethodStructureOutput} instance.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public final class MethodAnalyzer extends EmptyVisitor implements
		IBlockMethodVisitor {

	private static final int NO_LINE_INFO = -1;

	private final IMethodStructureOutput structureOutput;

	private int instructionCount;

	private int currentLine;

	private final IntSet lineNumbers;

	/**
	 * Creates a new analyzer that reports to the given
	 * {@link IMethodStructureOutput} instance.
	 * 
	 * @param structureOutput
	 *            consumer for method structure events
	 */
	public MethodAnalyzer(IMethodStructureOutput structureOutput) {
		this.structureOutput = structureOutput;
		this.instructionCount = 0;
		this.currentLine = NO_LINE_INFO;
		this.lineNumbers = new IntSet();
	}

	private void addInstruction() {
		instructionCount++;
		if (currentLine != NO_LINE_INFO) {
			lineNumbers.add(currentLine);
		}
	}

	// === MethodVisitor ===

	@Override
	public void visitLineNumber(int line, Label start) {
		currentLine = line;
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		addInstruction();
	}

	@Override
	public void visitInsn(int opcode) {
		addInstruction();
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		addInstruction();
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		addInstruction();
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		addInstruction();
	}

	@Override
	public void visitLdcInsn(Object cst) {
		addInstruction();
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		addInstruction();
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		addInstruction();
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		addInstruction();
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label[] labels) {
		addInstruction();
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		addInstruction();
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		addInstruction();
	}

	@Override
	public void visitEnd() {
		structureOutput.end();
	}

	// === IBlockVisitor ===

	public void visitBlockEndBeforeJump(int id) {
	}

	public void visitBlockEnd(int id) {
		structureOutput.block(id, instructionCount, lineNumbers.toArray());
		instructionCount = 0;
		lineNumbers.clear();
	}

}
