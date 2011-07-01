/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.flow.IMethodProbesVisitor;
import org.jacoco.core.internal.flow.Instruction;
import org.jacoco.core.internal.flow.LabelInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;

/**
 * A {@link IMethodProbesVisitor} that analyzes which statements and branches of
 * a method has been executed based on given probe data.
 */
public class MethodAnalyzer implements IMethodProbesVisitor {

	private final boolean[] executionData;

	private final MethodCoverageImpl coverage;

	private int currentLine = ISourceNode.UNKNOWN_LINE;

	private int firstLine = ISourceNode.UNKNOWN_LINE;

	private int lastLine = ISourceNode.UNKNOWN_LINE;

	// Due to ASM issue #315745 there can be more than one label per instruction
	private final List<Label> currentLabel = new ArrayList<Label>(2);

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
		this.coverage = new MethodCoverageImpl(name, desc, signature);
	}

	/**
	 * Returns the coverage data for this method after this visitor has been
	 * processed.
	 * 
	 * @return coverage data for this method
	 */
	public IMethodCoverage getCoverage() {
		return coverage;
	}

	public void visitLabel(final Label label) {
		currentLabel.add(label);
		if (!LabelInfo.isSuccessor(label)) {
			lastInsn = null;
		}
	}

	public void visitLineNumber(final int line, final Label start) {
		currentLine = line;
		if (firstLine > line) {
			firstLine = line;
		}
		if (lastLine < line || lastLine == ISourceNode.UNKNOWN_LINE) {
			lastLine = line;
		}
	}

	private void visitInsn() {
		final Instruction insn = new Instruction(currentLine);
		instructions.add(insn);
		if (lastInsn != null) {
			insn.setPredecessor(lastInsn);
		}
		final int labelCount = currentLabel.size();
		if (labelCount > 0) {
			for (int i = labelCount; --i >= 0;) {
				LabelInfo.setInstruction(currentLabel.get(i), insn);
			}
			currentLabel.clear();
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
		addProbe(probeId);
		lastInsn = null;
	}

	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId) {
		visitInsn();
		addProbe(probeId);
	}

	public void visitInsnWithProbe(final int opcode, final int probeId) {
		visitInsn();
		addProbe(probeId);
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
				addProbe(id);
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
		coverage.ensureCapacity(firstLine, lastLine);
		for (final Instruction i : instructions) {
			final int total = i.getBranches();
			final int covered = i.getCoveredBranches();
			final ICounter instrCounter = covered == 0 ? CounterImpl.COUNTER_1_0
					: CounterImpl.COUNTER_0_1;
			final ICounter branchCounter = total > 1 ? CounterImpl.getInstance(
					total - covered, covered) : CounterImpl.COUNTER_0_0;
			coverage.increment(instrCounter, branchCounter, i.getLine());
		}
		coverage.incrementMethodCounter();
	}

	public AnnotationVisitor visitAnnotationDefault() {
		// nothing to do
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

	public void visitAttribute(final Attribute attr) {
		// nothing to do
	}

	public void visitCode() {
		// nothing to do
	}

	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {
		// nothing to do
	}

	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		// nothing to do
	}

	public void visitMaxs(final int maxStack, final int maxLocals) {
		// nothing to do
	}

	private void addProbe(final int probeId) {
		lastInsn.addBranch();
		if (executionData != null && executionData[probeId]) {
			coveredProbes.add(lastInsn);
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
