/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Method visitor to collect flow related information about the {@link Label}s
 * within a class. It calculates the properties "multitarget" and "successor"
 * that can afterwards be obtained via {@link LabelInfo}.
 */
public final class LabelFlowAnalyzer implements MethodVisitor {

	/**
	 * <code>true</code> if the current instruction is a potential successor of
	 * the previous instruction. Accessible for testing.
	 */
	boolean successor = false;

	/**
	 * <code>true</code> for the very first instruction only. Accessible for
	 * testing.
	 */
	boolean first = true;

	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		// Enforce probes at the beginning and end of the block:
		LabelInfo.setTarget(start);
		LabelInfo.setTarget(handler);
	}

	public void visitJumpInsn(final int opcode, final Label label) {
		LabelInfo.setTarget(label);
		if (opcode == Opcodes.JSR) {
			throw new AssertionError("Subroutines not supported.");
		}
		successor = opcode != Opcodes.GOTO;
		first = false;
	}

	public void visitLabel(final Label label) {
		if (first) {
			LabelInfo.setTarget(label);
		}
		if (successor) {
			LabelInfo.setSuccessor(label);
		}
	}

	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		visitSwitchInsn(dflt, labels);
	}

	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		visitSwitchInsn(dflt, labels);
	}

	private void visitSwitchInsn(final Label dflt, final Label[] labels) {
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		setTargetIfNotDone(dflt);
		for (final Label l : labels) {
			setTargetIfNotDone(l);
		}
		successor = false;
		first = false;
	}

	private static void setTargetIfNotDone(final Label label) {
		if (!LabelInfo.isDone(label)) {
			LabelInfo.setTarget(label);
			LabelInfo.setDone(label);
		}
	}

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
		first = false;
	}

	public void visitIntInsn(final int opcode, final int operand) {
		successor = true;
		first = false;
	}

	public void visitVarInsn(final int opcode, final int var) {
		successor = true;
		first = false;
	}

	public void visitTypeInsn(final int opcode, final String type) {
		successor = true;
		first = false;
	}

	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		successor = true;
		first = false;
	}

	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		successor = true;
		first = false;
	}

	public void visitLdcInsn(final Object cst) {
		successor = true;
		first = false;
	}

	public void visitIincInsn(final int var, final int increment) {
		successor = true;
		first = false;
	}

	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		successor = true;
		first = false;
	}

	public void visitAttribute(final Attribute attr) {
		// nothing to do
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		// nothing to do
		return null;
	}

	public AnnotationVisitor visitParameterAnnotation(final int parameter,
			final String desc, final boolean visible) {
		// nothing to do
		return null;
	}

	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		// nothing to do
	}

	public void visitCode() {
		// nothing to do
	}

	public void visitLineNumber(final int line, final Label start) {
		// nothing to do
	}

	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {
		// nothing to do
	}

	public void visitMaxs(final int maxStack, final int maxLocals) {
		// nothing to do
	}

	public void visitEnd() {
		// nothing to do
	}

}
