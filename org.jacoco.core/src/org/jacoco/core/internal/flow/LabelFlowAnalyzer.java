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

/**
 * Method visitor to collect flow related information about the {@link Label}s
 * within a class. It calculates the properties "multitarget" and "successor"
 * that can afterwards be obtained via {@link LabelInfo}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
final class LabelFlowAnalyzer extends SuccessorAnalyzer {

	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		LabelInfo.setTarget(handler);
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		LabelInfo.setTarget(label);
		super.visitJumpInsn(opcode, label);
	}

	public void visitLabel(final Label label) {
		if (successor) {
			LabelInfo.setSuccessor(label);
		}
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		visitSwitchInsn(dflt, labels);
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		visitSwitchInsn(dflt, labels);
		super.visitLookupSwitchInsn(dflt, keys, labels);
	}

	private static void visitSwitchInsn(final Label dflt, final Label[] labels) {
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		setTargetIfNotDone(dflt);
		for (final Label l : labels) {
			setTargetIfNotDone(l);
		}
	}

	private static void setTargetIfNotDone(final Label label) {
		if (!LabelInfo.isDone(label)) {
			LabelInfo.setTarget(label);
			LabelInfo.setDone(label);
		}
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
