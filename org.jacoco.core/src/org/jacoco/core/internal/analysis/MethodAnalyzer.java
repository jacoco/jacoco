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
package org.jacoco.core.internal.analysis;

import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.CounterImpl;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.MethodCoverage;
import org.jacoco.core.internal.flow.IMethodProbesVisitor;
import org.jacoco.core.internal.flow.Instruction;
import org.jacoco.core.internal.flow.LabelInfo;
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
public class MethodAnalyzer implements IMethodProbesVisitor {

	private final boolean[] executionData;

	private final MethodCoverage coverage;

	private int currentLine = MethodCoverage.UNKNOWN_LINE;

	private Label currentLabel = null;

	/** List of all analyzed instructions */
	private final List<Instruction> instructions = new ArrayList<Instruction>();

	/** List of all predecessors of covered probes */
	private final List<Instruction> coveredProbes = new ArrayList<Instruction>();

	/** List of all jumps encountered */
	private final List<Jump> jumps = new ArrayList<Jump>();

	/** Last instruction in byte code sequence */
	private Instruction lastInsn;

	/**
	 * New Method analyzer for the given probe data.
	 * 
	 * @param name
	 *            method name
	 * @param desc
	 *            description of the method
	 * @param signature
	 *            optional parameterized signature
	 * 
	 * @param executionData
	 *            recorded probe date of the containing class or
	 *            <code>null</code> if the class is not executed at all
	 */
	public MethodAnalyzer(final String name, final String desc,
			final String signature, final boolean[] executionData) {
		this.executionData = executionData;
		this.coverage = new MethodCoverage(name, desc, signature);
	}

	/**
	 * Returns the coverage data for this method after this visitor has been
	 * processed.
	 * 
	 * @return coverage data for this method
	 */
	public MethodCoverage getCoverage() {
		return coverage;
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
		final Instruction insn = new Instruction(currentLine);
		instructions.add(insn);
		if (lastInsn != null) {
			insn.setPredecessor(lastInsn);
		}
		if (currentLabel != null) {
			LabelInfo.setInstruction(currentLabel, insn);
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
		LabelInfo.resetDone(labels);
		jumps.add(new Jump(lastInsn, dflt));
		LabelInfo.setDone(dflt);
		for (final Label l : labels) {
			if (!LabelInfo.isDone(l)) {
				jumps.add(new Jump(lastInsn, l));
				LabelInfo.setDone(l);
			}
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
		if (!LabelInfo.isDone(label)) {
			if (id == LabelInfo.NO_PROBE) {
				jumps.add(new Jump(lastInsn, label));
			} else {
				addProbe(lastInsn, id);
			}
			LabelInfo.setDone(label);
		}
	}

	public void visitEnd() {
		// Wire jumps:
		for (final Jump j : jumps) {
			LabelInfo.getInstruction(j.target).setPredecessor(j.source);
		}
		// Propagate probe values:
		for (final Instruction p : coveredProbes) {
			p.setCovered();
		}
		// Report result:
		for (final Instruction i : instructions) {
			final int total = i.getBranches();
			final int covered = i.getCoveredBranches();
			final ICounter instructions = covered == 0 ? CounterImpl.COUNTER_1_0
					: CounterImpl.COUNTER_0_1;
			final ICounter branches = total > 1 ? CounterImpl.getInstance(total
					- covered, covered) : CounterImpl.COUNTER_0_0;
			coverage.increment(instructions, branches, i.getLine());
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

	private void addProbe(final Instruction predecessor, final int probeId) {
		predecessor.addBranch();
		if (executionData != null && executionData[probeId]) {
			coveredProbes.add(predecessor);
		}
	}

	private static class Jump {

		final Instruction source;
		final Label target;

		Jump(final Instruction source, final Label target) {
			this.source = source;
			this.target = target;
		}
	}

}
