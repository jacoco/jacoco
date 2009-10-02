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
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;

/**
 * This {@link IBlockMethodVisitor} analyzes the block structure of a method and
 * reports it to a {@link IMethodStructureVisitor} instance.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public final class MethodAnalyzer implements IBlockMethodVisitor {

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

	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		return null;
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	public AnnotationVisitor visitParameterAnnotation(final int parameter,
			final String desc, final boolean visible) {
		return null;
	}

	public void visitAttribute(final Attribute attr) {
	}

	public void visitCode() {
	}

	public void visitFrame(final int type, final int local,
			final Object[] local2, final int stack, final Object[] stack2) {
	}

	public void visitLabel(final Label label) {
	}

	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
	}

	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
	}

	public void visitMaxs(final int maxStack, final int maxLocals) {
	}

	public void visitLineNumber(final int line, final Label start) {
		currentLine = line;
	}

	public void visitInsn(final int opcode) {
		addInstruction();
	}

	public void visitJumpInsn(final int opcode, final Label label) {
		addInstruction();
	}

	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		addInstruction();
	}

	public void visitIincInsn(final int var, final int increment) {
		addInstruction();
	}

	public void visitIntInsn(final int opcode, final int operand) {
		addInstruction();
	}

	public void visitLdcInsn(final Object cst) {
		addInstruction();
	}

	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		addInstruction();
	}

	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		addInstruction();
	}

	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		addInstruction();
	}

	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		addInstruction();
	}

	public void visitTypeInsn(final int opcode, final String type) {
		addInstruction();
	}

	public void visitVarInsn(final int opcode, final int var) {
		addInstruction();
	}

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
