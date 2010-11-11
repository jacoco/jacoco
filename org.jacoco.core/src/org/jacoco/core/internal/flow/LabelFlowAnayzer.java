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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Method visitor to collect flow related information about the {@link Label}s
 * within a class. It calculates the properties "multitarget" and "successor"
 * that can afterwards be obtained via {@link LabelInfo}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
final class LabelFlowAnayzer implements MethodVisitor {

	private static void setTarget(final Label[] labels) {
		for (final Label l : labels) {
			LabelInfo.setTarget(l);
		}
	}

	// visible for testing
	/* package */boolean successor = true;

	// === MethodVisitor ===

	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		LabelInfo.setTarget(handler);
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
		LabelInfo.setTarget(label);
		successor = opcode != Opcodes.GOTO;
	}

	public void visitLabel(final Label label) {
		if (successor) {
			LabelInfo.setSuccessor(label);
		}
	}

	public void visitLdcInsn(final Object cst) {
		successor = true;
	}

	public void visitIincInsn(final int var, final int increment) {
		successor = true;
	}

	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		// FIXME The same label instances must be flagged once only
		LabelInfo.setTarget(dflt);
		setTarget(labels);
		successor = false;
	}

	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		// FIXME The same label instances must be flagged once only
		LabelInfo.setTarget(dflt);
		setTarget(labels);
		successor = false;
	}

	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		successor = true;
	}

	// Not relevant:

	public void visitAttribute(final Attribute attr) {
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		return null;
	}

	public AnnotationVisitor visitParameterAnnotation(final int parameter,
			final String desc, final boolean visible) {
		return null;
	}

	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
	}

	public void visitCode() {
	}

	public void visitLineNumber(final int line, final Label start) {
	}

	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {
	}

	public void visitMaxs(final int maxStack, final int maxLocals) {
	}

	public void visitEnd() {
	}

}
