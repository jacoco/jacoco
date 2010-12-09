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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

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

	/** List of all analyzed instructions */
	private final List<Insn> instructions = new ArrayList<Insn>();

	/** Last instruction in byte code sequence */
	private Insn lastInsn;

	/**
	 * Jump instruction to the given label
	 * 
	 * TODO: Replace Map by LabelInfo
	 */
	private final Map<Label, Insn> jumpInsn = new HashMap<Label, Insn>();

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

	public void visitLabel(final Label label) {
		final Insn insn = jumpInsn.remove(label);
		if (insn != null) {
			lastInsn = insn;
		}
	}

	public void visitLineNumber(final int line, final Label start) {
		currentLine = line;
	}

	private void visitInsn() {
		final Insn insn = new Insn(currentLine, lastInsn);
		instructions.add(insn);
		lastInsn = insn;
	}

	public void visitInsn(final int opcode) {
		visitInsn();
	}

	public void visitIntInsn(final int opcode, final int operand) {
		visitInsn();
	}

	public void visitVarInsn(final int opcode, final int var) {
		visitInsn();
	}

	public void visitTypeInsn(final int opcode, final String type) {
		visitInsn();
	}

	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		visitInsn();
	}

	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		visitInsn();
	}

	public void visitJumpInsn(final int opcode, final Label label) {
		visitInsn();
		jumpInsn.put(label, lastInsn);
	}

	public void visitLdcInsn(final Object cst) {
		visitInsn();
	}

	public void visitIincInsn(final int var, final int increment) {
		visitInsn();
	}

	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		visitInsn();
	}

	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		visitInsn();
	}

	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		visitInsn();
	}

	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		visitInsn();
	}

	public void visitProbe(final int probeId) {
		if (probes[probeId]) {
			lastInsn.setCovered();
		}
		lastInsn = null;
	}

	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId) {
		visitInsn();
		if (probes[probeId]) {
			lastInsn.setCovered();
		}
		if (opcode == Opcodes.GOTO) {
			lastInsn = null;
		}
	}

	public void visitInsnWithProbe(final int opcode, final int probeId) {
		visitInsn();
		if (probes[probeId]) {
			lastInsn.setCovered();
		}
		lastInsn = null;
	}

	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels) {
		// TODO Auto-generated method stub

	}

	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels) {
		// TODO Auto-generated method stub

	}

	public void visitEnd() {
		for (final Insn i : instructions) {
			i.process(output);
		}
	}

	// === nothing to do here ===

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

	public void visitAttribute(final Attribute attr) {
	}

	public void visitCode() {
	}

	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {
	}

	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
	}

	public void visitMaxs(final int maxStack, final int maxLocals) {
	}

	private static class Insn {

		private final int line;
		private final Insn predecessor;
		private boolean covered;

		Insn(final int line, final Insn predecessor) {
			this.line = line;
			this.predecessor = predecessor;
		}

		void setCovered() {
			covered = true;
			if (predecessor != null) {
				predecessor.setCovered();
			}
		}

		void process(final Output output) {
			output.visitInsn(covered, line);
		}

	}

}
