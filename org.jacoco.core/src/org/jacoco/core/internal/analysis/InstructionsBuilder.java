/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.flow.LabelInfo;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stateful builder for the {@link Instruction}s of a method. All instructions
 * of a method must be added in their original sequence along with additional
 * information like line numbers. Afterwards the instructions can be obtained
 * with the <code>getInstructions()</code> method.
 */
class InstructionsBuilder {

	/** Probe array of the class the analyzed method belongs to. */
	private final boolean[] probes;

	/** The line which belong to subsequently added instructions. */
	private int currentLine;

	/** The last instruction which has been added. */
	private Instruction currentInsn;

	/**
	 * All instructions of a method mapped from the ASM node to the
	 * corresponding {@link Instruction} instance.
	 */
	private final Map<AbstractInsnNode, Instruction> instructions;

	/**
	 * The labels which mark the subsequent instructions.
	 *
	 * Due to ASM issue #315745 there can be more than one label per instruction
	 */
	private final List<Label> currentLabel;

	/**
	 * List of all jumps within the control flow. We need to store jumps
	 * temporarily as the target {@link Instruction} may not been known yet.
	 */
	private final List<Jump> jumps;

	/**
	 * Creates a new builder instance which can be used to analyze a single
	 * method.
	 *
	 * @param probes
	 *            probe array of the corresponding class used to determine the
	 *            coverage status of every instruction.
	 */
	InstructionsBuilder(final boolean[] probes) {
		this.probes = probes;
		this.currentLine = ISourceNode.UNKNOWN_LINE;
		this.currentInsn = null;
		this.instructions = new HashMap<AbstractInsnNode, Instruction>();
		this.currentLabel = new ArrayList<Label>(2);
		this.jumps = new ArrayList<Jump>();
	}

	/**
	 * Sets the current source line. All subsequently added instructions will be
	 * assigned to this line. If no line is set (e.g. for classes compiled
	 * without debug information) {@link ISourceNode#UNKNOWN_LINE} is assigned
	 * to the instructions.
	 */
	void setCurrentLine(final int line) {
		currentLine = line;
	}

	/**
	 * Adds a label which applies to the subsequently added instruction. Due to
	 * ASM internals multiple {@link Label}s can be added to an instruction.
	 */
	void addLabel(final Label label) {
		currentLabel.add(label);
		if (!LabelInfo.isSuccessor(label)) {
			noSuccessor();
		}
	}

	/**
	 * Adds a new instruction. Instructions are by default linked with the
	 * previous instruction unless specified otherwise.
	 */
	void addInstruction(final AbstractInsnNode node) {
		final Instruction insn = new Instruction(currentLine);
		final int labelCount = currentLabel.size();
		if (labelCount > 0) {
			for (int i = labelCount; --i >= 0;) {
				LabelInfo.setInstruction(currentLabel.get(i), insn);
			}
			currentLabel.clear();
		}
		if (currentInsn != null) {
			currentInsn.addBranch(insn, 0);
		}
		currentInsn = insn;
		instructions.put(node, insn);
	}

	/**
	 * Declares that the next instruction will not be a successor of the current
	 * instruction. This is the case with an unconditional jump or technically
	 * when a probe was inserted before.
	 */
	void noSuccessor() {
		currentInsn = null;
	}

	/**
	 * Adds a jump from the last added instruction.
	 *
	 * @param target
	 *            jump target
	 * @param branch
	 *            unique branch number
	 */
	void addJump(final Label target, final int branch) {
		jumps.add(new Jump(currentInsn, target, branch));
	}

	/**
	 * Adds a new probe for the last instruction.
	 *
	 * @param probeId
	 *            index in the probe array
	 * @param branch
	 *            unique branch number for the last instruction
	 */
	void addProbe(final int probeId, final int branch) {
		final boolean executed = probes != null && probes[probeId];
		currentInsn.addBranch(executed, branch);
	}

	/**
	 * Returns the status for all instructions of this method. This method must
	 * be called exactly once after the instructions have been added.
	 *
	 * @return map of ASM instruction nodes to corresponding {@link Instruction}
	 *         instances
	 */
	Map<AbstractInsnNode, Instruction> getInstructions() {
		// Wire jumps:
		for (final Jump j : jumps) {
			j.wire();
		}

		return instructions;
	}

	private static class Jump {

		private final Instruction source;
		private final Label target;
		private final int branch;

		Jump(final Instruction source, final Label target, final int branch) {
			this.source = source;
			this.target = target;
			this.branch = branch;
		}

		void wire() {
			source.addBranch(LabelInfo.getInstruction(target), branch);
		}

	}

}
