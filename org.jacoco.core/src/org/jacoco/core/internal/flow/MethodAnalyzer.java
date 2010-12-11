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
		 * Called for every branching point.
		 * 
		 * @param missed
		 *            number of missed branches
		 * @param covered
		 *            number of covered branches
		 * @param line
		 *            source line number of the instruction
		 */
		public void visitBranches(int missed, int covered, int line);
	}

	private final boolean[] executionData;

	private final Output output;

	private int currentLine = Output.UNKNOWN_LINE;

	private Label currentLabel = null;

	/** List of all analyzed instructions */
	private final List<Insn> instructions = new ArrayList<Insn>();

	/** List of all predecessors of covered probes */
	private final List<Insn> coveredProbes = new ArrayList<Insn>();

	/** List of all jumps encountered */
	private final List<Jump> jumps = new ArrayList<Jump>();

	/** Last instruction in byte code sequence */
	private Insn lastInsn;

	/**
	 * Mapping from labels to addressed instruction.
	 * 
	 * TODO: Replace Map by LabelInfo
	 */
	private final Map<Label, Insn> labels = new HashMap<Label, Insn>();

	/**
	 * New Method analyzer for the given probe data.
	 * 
	 * @param executionData
	 *            recorded probe date of the containing class
	 * @param output
	 *            instance to report coverage information to
	 */
	public MethodAnalyzer(final boolean[] executionData, final Output output) {
		this.executionData = executionData;
		this.output = output;
	}

	public void visitLabel(final Label label) {
		currentLabel = label;
		if (!LabelInfo.isSuccessor(label)) {
			lastInsn = null;
		}
	}

	public void visitLineNumber(final int line, final Label start) {
		currentLine = line;
	}

	private void visitInsn() {
		final Insn insn = new Insn(currentLine);
		instructions.add(insn);
		if (lastInsn != null) {
			insn.setPredecessor(lastInsn);
		}
		if (currentLabel != null) {
			labels.put(currentLabel, insn);
			currentLabel = null;
		}
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
		jumps.add(new Jump(lastInsn, label));
	}

	public void visitLdcInsn(final Object cst) {
		visitInsn();
	}

	public void visitIincInsn(final int var, final int increment) {
		visitInsn();
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
		visitInsn();
		jumps.add(new Jump(lastInsn, dflt));
		for (final Label l : labels) {
			jumps.add(new Jump(lastInsn, l));
		}
	}

	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		visitInsn();
	}

	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		visitInsn();
	}

	public void visitProbe(final int probeId) {
		addProbe(lastInsn, probeId);
		lastInsn = null;
	}

	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId) {
		visitInsn();
		addProbe(lastInsn, probeId);
	}

	public void visitInsnWithProbe(final int opcode, final int probeId) {
		visitInsn();
		addProbe(lastInsn, probeId);
	}

	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels) {
		visitSwitchInsnWithProbes(dflt, labels);
	}

	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels) {
		visitSwitchInsnWithProbes(dflt, labels);
	}

	private void visitSwitchInsnWithProbes(final Label dflt,
			final Label[] labels) {
		visitInsn();
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		visitSwitchTarget(dflt);
		for (final Label l : labels) {
			visitSwitchTarget(l);
		}
	}

	private void visitSwitchTarget(final Label label) {
		final int id = LabelInfo.getProbeId(label);
		if (id == LabelInfo.NO_PROBE) {
			jumps.add(new Jump(lastInsn, label));
		} else {
			if (!LabelInfo.isDone(label)) {
				addProbe(lastInsn, id);
				LabelInfo.setDone(label);
			}
		}
	}

	public void visitEnd() {
		// Wire jumps:
		for (final Jump j : jumps) {
			labels.get(j.target).setPredecessor(j.source);
		}
		// Propagate probe values:
		for (final Insn p : coveredProbes) {
			p.setCovered();
		}
		// Report result:
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
		private int branches;
		private int coveredBranches;
		private Insn predecessor;

		Insn(final int line) {
			this.line = line;
			this.branches = 0;
			this.coveredBranches = 0;
		}

		public void setPredecessor(final Insn predecessor) {
			this.predecessor = predecessor;
			predecessor.addBranch();
		}

		public void addBranch() {
			branches++;
		}

		public void setCovered() {
			if (coveredBranches == 0) {
				if (predecessor != null) {
					predecessor.setCovered();
				}
			}
			coveredBranches++;
		}

		void process(final Output output) {
			output.visitInsn(coveredBranches > 0, line);
			if (branches > 1) {
				output.visitBranches(branches - coveredBranches,
						coveredBranches, line);
			}
		}
	}

	private void addProbe(final Insn predecessor, final int probeId) {
		predecessor.addBranch();
		if (executionData[probeId]) {
			coveredProbes.add(predecessor);
		}
	}

	private static class Jump {

		final Insn source;
		final Label target;

		Jump(final Insn source, final Label target) {
			this.source = source;
			this.target = target;
		}
	}

}
