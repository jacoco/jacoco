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

import org.jacoco.core.data.IMethodStructureVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * This {@link IBlockMethodVisitor} analyzes the block structure of a method and
 * reports it to a {@link IMethodStructureVisitor} instance.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public final class MethodAnalyzer extends EmptyVisitor implements
		IBlockMethodVisitor {

	private static final int NO_LINE_INFO = -1;

	private final IMethodStructureVisitor structureVisitor;

	private int instructionCount;

	private int currentLine;

	private final IntSet lineNumbers;

	/**
	 * Creates a new analyzer that reports to the given
	 * {@link IMethodStructureVisitor} instance.
	 * 
	 * @param structureVisitor
	 *            consumer for method structure events
	 */
	public MethodAnalyzer(final IMethodStructureVisitor structureVisitor) {
		this.structureVisitor = structureVisitor;
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
	public void visitLineNumber(final int line, final Label start) {
		currentLine = line;
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		addInstruction();
	}

	@Override
	public void visitInsn(final int opcode) {
		addInstruction();
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		addInstruction();
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		addInstruction();
	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
		addInstruction();
	}

	@Override
	public void visitLdcInsn(final Object cst) {
		addInstruction();
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		addInstruction();
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		addInstruction();
	}

	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		addInstruction();
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		addInstruction();
	}

	@Override
	public void visitTypeInsn(final int opcode, final String type) {
		addInstruction();
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		addInstruction();
	}

	@Override
	public void visitEnd() {
		structureVisitor.visitEnd();
	}

	// === IBlockVisitor ===

	public void visitBlockEndBeforeJump(final int id) {
	}

	public void visitBlockEnd(final int id) {
		structureVisitor.block(id, instructionCount, lineNumbers.toArray());
		instructionCount = 0;
		lineNumbers.clear();
	}

}
