/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

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

	/** Instruction index within a method to avoid ambiguity. */
	private int instructionIndex;

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
		this.instructionIndex = 0;
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
		final String sign = buildInstructionSign(node, instructionIndex++);
		final Instruction insn = new Instruction(currentLine, sign);
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

	private static String buildInstructionSign(final AbstractInsnNode node,
			final int index) {
		final StringBuilder sb = new StringBuilder(64);
		sb.append(node.getType()).append('#').append(node.getOpcode());
		switch (node.getType()) {
		case AbstractInsnNode.INT_INSN: {
			final IntInsnNode n = (IntInsnNode) node;
			sb.append('#').append(n.operand);
			break;
		}
		case AbstractInsnNode.VAR_INSN: {
			final VarInsnNode n = (VarInsnNode) node;
			sb.append('#').append(n.var);
			break;
		}
		case AbstractInsnNode.TYPE_INSN: {
			final TypeInsnNode n = (TypeInsnNode) node;
			sb.append('#').append(n.desc);
			break;
		}
		case AbstractInsnNode.FIELD_INSN: {
			final FieldInsnNode n = (FieldInsnNode) node;
			sb.append('#').append(n.owner).append('#').append(n.name)
					.append('#').append(n.desc);
			break;
		}
		case AbstractInsnNode.METHOD_INSN: {
			final MethodInsnNode n = (MethodInsnNode) node;
			sb.append('#').append(n.owner).append('#').append(n.name)
					.append('#').append(n.desc).append('#').append(n.itf);
			break;
		}
		case AbstractInsnNode.INVOKE_DYNAMIC_INSN: {
			final InvokeDynamicInsnNode n = (InvokeDynamicInsnNode) node;
			sb.append('#').append(n.name).append('#').append(n.desc);
			if (n.bsm != null) {
				final Handle h = n.bsm;
				sb.append('#').append(h.getTag()).append('#')
						.append(h.getOwner()).append('#').append(h.getName())
						.append('#').append(h.getDesc()).append('#')
						.append(h.isInterface());
			}
			if (n.bsmArgs != null) {
				for (final Object arg : n.bsmArgs) {
					sb.append('#').append(formatArg(arg));
				}
			}
			break;
		}
		case AbstractInsnNode.JUMP_INSN: {
			// opcode already included; label is unstable across versions
			break;
		}
		case AbstractInsnNode.LDC_INSN: {
			final LdcInsnNode n = (LdcInsnNode) node;
			sb.append('#').append(formatArg(n.cst));
			break;
		}
		case AbstractInsnNode.IINC_INSN: {
			final IincInsnNode n = (IincInsnNode) node;
			sb.append('#').append(n.var).append('#').append(n.incr);
			break;
		}
		case AbstractInsnNode.TABLESWITCH_INSN: {
			final TableSwitchInsnNode n = (TableSwitchInsnNode) node;
			sb.append('#').append(n.min).append('#').append(n.max);
			break;
		}
		case AbstractInsnNode.LOOKUPSWITCH_INSN: {
			final LookupSwitchInsnNode n = (LookupSwitchInsnNode) node;
			if (n.keys != null) {
				for (final Object k : n.keys) {
					sb.append('#').append(k);
				}
			}
			break;
		}
		case AbstractInsnNode.MULTIANEWARRAY_INSN: {
			final MultiANewArrayInsnNode n = (MultiANewArrayInsnNode) node;
			sb.append('#').append(n.desc).append('#').append(n.dims);
			break;
		}
		default:
			break;
		}
		sb.append('#').append(index);
		return sb.toString();
	}

	private static String formatArg(final Object arg) {
		if (arg == null) {
			return "null";
		}
		if (arg instanceof Type) {
			return ((Type) arg).getDescriptor();
		}
		return String.valueOf(arg);
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
