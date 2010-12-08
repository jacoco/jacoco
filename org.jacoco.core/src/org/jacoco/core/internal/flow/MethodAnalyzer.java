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
 * A {@link IMethodProbesVisitor} that analyzes which statements and branches of
 * a method has been executed based on given probe data.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public final class MethodAnalyzer implements IMethodProbesVisitor {

	/**
	 * The result for each instruction and branch of the analysis is reported to
	 * an instance of this interface.
	 */
	public interface Output {

		/** Place holder for unknown lines (no debug information) */
		public static int UNKNOWN_LINE = -1;

		/**
		 * Called for every instruction.
		 * 
		 * @param covered
		 *            <code>true</code> if the instruction has been executed
		 * @param line
		 *            source line number of the instruction
		 */
		public void visitInsn(boolean covered, int line);

		/**
		 * Called for every branch.
		 * 
		 * @param covered
		 *            <code>true</code> if the branch has been executed
		 * @param line
		 *            source line number of the instruction
		 */
		public void visitBranch(boolean covered, int line);
	}

	private final boolean[] probes;

	private final Output output;

	private int currentLine = Output.UNKNOWN_LINE;

	/**
	 * New Method analyzer for the given probe data.
	 * 
	 * @param probes
	 *            recorded probe date of the containing class
	 * @param output
	 *            instance to report coverage information to
	 */
	public MethodAnalyzer(final boolean[] probes, final Output output) {
		this.probes = probes;
		this.output = output;
	}

	public void visitLineNumber(final int line, final Label start) {
		currentLine = line;
	}

	public void visitInsn(final int opcode) {
		// TODO Auto-generated method stub
	}

	public AnnotationVisitor visitAnnotationDefault() {
		// TODO Auto-generated method stub
		return null;
	}

	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		// TODO Auto-generated method stub
		return null;
	}

	public AnnotationVisitor visitParameterAnnotation(final int parameter,
			final String desc, final boolean visible) {
		// TODO Auto-generated method stub
		return null;
	}

	public void visitAttribute(final Attribute attr) {
		// TODO Auto-generated method stub

	}

	public void visitCode() {
		// TODO Auto-generated method stub

	}

	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {
		// TODO Auto-generated method stub

	}

	public void visitIntInsn(final int opcode, final int operand) {
		// TODO Auto-generated method stub

	}

	public void visitVarInsn(final int opcode, final int var) {
		// TODO Auto-generated method stub

	}

	public void visitTypeInsn(final int opcode, final String type) {
		// TODO Auto-generated method stub

	}

	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		// TODO Auto-generated method stub

	}

	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		// TODO Auto-generated method stub

	}

	public void visitJumpInsn(final int opcode, final Label label) {
		// TODO Auto-generated method stub

	}

	public void visitLabel(final Label label) {
		// TODO Auto-generated method stub

	}

	public void visitLdcInsn(final Object cst) {
		// TODO Auto-generated method stub

	}

	public void visitIincInsn(final int var, final int increment) {
		// TODO Auto-generated method stub

	}

	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		// TODO Auto-generated method stub

	}

	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		// TODO Auto-generated method stub

	}

	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		// TODO Auto-generated method stub

	}

	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		// TODO Auto-generated method stub

	}

	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		// TODO Auto-generated method stub

	}

	public void visitMaxs(final int maxStack, final int maxLocals) {
		// TODO Auto-generated method stub

	}

	public void visitEnd() {
		// TODO Auto-generated method stub

	}

	public void visitProbe(final int probeId) {
		// TODO Auto-generated method stub

	}

	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId) {
		// TODO Auto-generated method stub

	}

	public void visitInsnWithProbe(final int opcode, final int probeId) {
		output.visitInsn(probes[probeId], currentLine);
	}

	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels) {
		// TODO Auto-generated method stub

	}

	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels) {
		// TODO Auto-generated method stub

	}

}
