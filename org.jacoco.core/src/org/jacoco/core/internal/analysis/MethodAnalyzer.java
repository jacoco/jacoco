/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.Instruction;
import org.jacoco.core.internal.flow.LabelInfo;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;

/**
 * A {@link MethodProbesVisitor} that analyzes which statements and branches of
 * a method has been executed based on given probe data.
 */
public class MethodAnalyzer extends MethodProbesVisitor {

	private final boolean[] probes;

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
	private Instruction lastInstruction;

	/**
	 * New Method analyzer for the given probe data.
	 * 
	 * @param name
	 *            method name
	 * @param descriptor
	 *            method descriptor
	 * @param signature
	 *            optional parameterized signature
	 * 
	 * @param probes
	 *            recorded probe date of the containing class or
	 *            <code>null</code> if the class is not executed at all
	 */
	public MethodAnalyzer(final String name, final String descriptor,
			final String signature, final boolean[] probes) {
		super();
		this.probes = probes;
		this.coverage = new MethodCoverageImpl(name, descriptor, signature);
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

	@Override
	public void visitLabel(final Label label) {
		currentLabel.add(label);
		if (!LabelInfo.isSuccessor(label)) {
			lastInstruction = null;
		}
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		currentLine = line;
		updateFirstLine(line);
		updateLastLine(line);
	}

	private void updateFirstLine(final int line) {
		if (lineIsBeforeFirstLine(line)) {
			firstLine = line;
		}
	}

	private boolean lineIsBeforeFirstLine(final int line) {
		return firstLine > line || firstLine == ISourceNode.UNKNOWN_LINE;
	}

	private void updateLastLine(final int line) {
		if (lineIsAfterLastLine(line)) {
			lastLine = line;
		}
	}

	private boolean lineIsAfterLastLine(final int line) {
		return lastLine < line;
	}

	private void visitInstruction() {
		final Instruction instruction = new Instruction(currentLine);
		instructions.add(instruction);
		if (lastInstruction != null) {
			instruction.setPredecessor(lastInstruction);
		}
		final int labelCount = currentLabel.size();
		if (labelCount > 0) {
			for (int i = labelCount; --i >= 0;) {
				LabelInfo.setInstruction(currentLabel.get(i), instruction);
			}
			currentLabel.clear();
		}
		lastInstruction = instruction;
	}

	@Override
	public void visitInsn(final int opcode) {
		visitInstruction();
	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
		visitInstruction();
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		visitInstruction();
	}

	@Override
	public void visitTypeInsn(final int opcode, final String type) {
		visitInstruction();
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String descriptor) {
		visitInstruction();
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String descriptor, final boolean itf) {
		visitInstruction();
	}

	@Override
	public void visitInvokeDynamicInsn(final String name,
			final String descriptor, final Handle bsm, final Object... bsmArgs) {
		visitInstruction();
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		visitInstruction();
		jumps.add(new Jump(lastInstruction, label));
	}

	@Override
	public void visitLdcInsn(final Object cst) {
		visitInstruction();
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		visitInstruction();
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label... labels) {
		visitSwitchInsn(dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		visitSwitchInsn(dflt, labels);
	}

	private void visitSwitchInsn(final Label dflt, final Label[] labels) {
		visitInstruction();
		LabelInfo.resetDone(labels);
		jumps.add(new Jump(lastInstruction, dflt));
		LabelInfo.setDone(dflt);
		for (final Label l : labels) {
			if (!LabelInfo.isDone(l)) {
				jumps.add(new Jump(lastInstruction, l));
				LabelInfo.setDone(l);
			}
		}
	}

	@Override
	public void visitMultiANewArrayInsn(final String descriptor,
			final int dimensions) {
		visitInstruction();
	}

	@Override
	public void visitProbe(final int probeId) {
		addProbe(probeId);
		lastInstruction = null;
	}

	@Override
	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId, final IFrame frame) {
		visitInstruction();
		addProbe(probeId);
	}

	@Override
	public void visitInsnWithProbe(final int opcode, final int probeId) {
		visitInstruction();
		addProbe(probeId);
	}

	@Override
	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels, final IFrame frame) {
		visitSwitchInsnWithProbes(dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels, final IFrame frame) {
		visitSwitchInsnWithProbes(dflt, labels);
	}

	private void visitSwitchInsnWithProbes(final Label dflt,
			final Label[] labels) {
		visitInstruction();
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		visitSwitchTarget(dflt);
		for (final Label label : labels) {
			visitSwitchTarget(label);
		}
	}

	private void visitSwitchTarget(final Label label) {
		final int id = LabelInfo.getProbeId(label);
		if (!LabelInfo.isDone(label)) {
			if (id == LabelInfo.NO_PROBE) {
				jumps.add(new Jump(lastInstruction, label));
			} else {
				addProbe(id);
			}
			LabelInfo.setDone(label);
		}
	}

	@Override
	public void visitEnd() {
		wireJumps();
		propagateProbeValues();
		reportResult();
	}

	private void wireJumps() {
		for (final Jump jump : jumps) {
			LabelInfo.getInstruction(jump.target).setPredecessor(jump.source);
		}
	}

	private void propagateProbeValues() {
		for (final Instruction probe : coveredProbes) {
			probe.setCovered();
		}
	}

	private void reportResult() {
		coverage.ensureCapacity(firstLine, lastLine);
		for (final Instruction instruction : instructions) {
			reportInstructionCoverage(instruction);
		}
		coverage.incrementMethodCounter();
	}

	private void reportInstructionCoverage(final Instruction instruction) {
		final int total = instruction.getBranches();
		final int covered = instruction.getCoveredBranches();
		final ICounter instrCounter = covered == 0 ? CounterImpl.COUNTER_1_0
				: CounterImpl.COUNTER_0_1;
		final ICounter branchCounter = total > 1 ? CounterImpl.getInstance(
				total - covered, covered) : CounterImpl.COUNTER_0_0;
		coverage.increment(instrCounter, branchCounter, instruction.getLine());
	}

	private void addProbe(final int probeId) {
		lastInstruction.addBranch();
		if (probes != null && probes[probeId]) {
			coveredProbes.add(lastInstruction);
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
