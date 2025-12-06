/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.LabelInfo;
import org.jacoco.core.internal.flow.MethodProbesAdapter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * A {@link MethodProbesVisitor} that builds the {@link Instruction}s of a
 * method to calculate the detailed execution status.
 */
public class MethodAnalyzer extends MethodProbesVisitor {

	private final InstructionsBuilder builder;

	/** Current node of the ASM tree API */
	private AbstractInsnNode currentNode;

	private int currentNo = 0;

	private static final String separator = "#";

	private int currentProbeId = 0;

	/**
	 * New instance that uses the given builder.
	 */
	MethodAnalyzer(final InstructionsBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void accept(final MethodNode methodNode,
			final MethodVisitor methodVisitor) {
		methodVisitor.visitCode();
		for (final TryCatchBlockNode n : methodNode.tryCatchBlocks) {
			currentNo++;
			currentProbeId = ((MethodProbesAdapter) methodVisitor)
					.getIdGenerator().getId();
			n.accept(methodVisitor);
		}
		// 依次遍历的method的instructions，type一样且顺序一样，则判断为同一个
		for (final AbstractInsnNode i : methodNode.instructions) {
			currentNode = i;
			currentNo++;
			currentProbeId = ((MethodProbesAdapter) methodVisitor)
					.getIdGenerator().getId();
			i.accept(methodVisitor);
		}
		methodVisitor.visitEnd();
	}

	@Override
	public void visitLabel(final Label label) {
		builder.addLabel(label);
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		// 指令Instruction的line的属性来源，ASM的行数
		builder.setCurrentLine(line);
	}

	@Override
	public void visitInsn(final int opcode) {
		String sign = opcode + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
		String sign = opcode + separator + operand + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		String sign = opcode + separator + var + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
	}

	@Override
	public void visitTypeInsn(final int opcode, final String type) {
		String sign = opcode + separator + type + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner,
							   final String name, final String desc) {
		String sign = opcode + separator + owner + separator + name + separator
				+ desc + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
								final String name, final String desc, final boolean itf) {
		String sign = opcode + separator + owner + separator + name + separator
				+ desc + separator + itf + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
	}

	@Override
	public void visitInvokeDynamicInsn(final String name, final String desc,
			final Handle bsm, final Object... bsmArgs) {
		StringBuilder signBuilder = new StringBuilder();
		if (bsmArgs != null) {
			for (int i = 0; i < bsmArgs.length; i++) {
				signBuilder.append(separator)
						.append(bsmArgs[i].getClass().getName());
			}
		}
		String sign = name + separator + desc + separator + bsm.toString()
				+ separator + signBuilder.toString() + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		String sign = opcode + separator + getLableString(label) + separator
				+ currentNo;
		builder.addInstruction(currentNode, sign, LabelInfo.getProbeId(label));
		builder.addJump(label, 1);
	}

	public String getLableString(Label label) {
		boolean multiTarget = LabelInfo.isMultiTarget(label);
		boolean isSuccessor = LabelInfo.isSuccessor(label);
		boolean isDone = LabelInfo.isDone(label);
		boolean isMethodInvocationLile = LabelInfo
				.isMethodInvocationLine(label);
		return multiTarget + separator + isSuccessor + separator + isDone
				+ separator + isMethodInvocationLile;
	}

	@Override
	public void visitLdcInsn(final Object cst) {
		// cst是一个常量类型
		String sign = cst.toString() + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		String sign = var + separator + increment + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
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
		String label1 = getLableString(dflt);
		for (final Label l : labels) {
			label1 += separator + getLableString(l);
		}
		String sign = label1 + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
		LabelInfo.resetDone(labels);
		int branch = 0;
		builder.addJump(dflt, branch);
		LabelInfo.setDone(dflt);
		for (final Label l : labels) {
			if (!LabelInfo.isDone(l)) {
				branch++;
				builder.addJump(l, branch);
				LabelInfo.setDone(l);
			}
		}
	}

	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		String sign = desc + separator + dims + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
	}

	@Override
	public void visitProbe(final int probeId) {
		builder.addProbe(probeId, 0);
		builder.noSuccessor();
	}

	@Override
	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId, final IFrame frame) {
		String sign = opcode + separator + getLableString(label) + separator
				+ frame.getClass().getName() + currentNo;
		builder.addInstruction(currentNode, sign, probeId);
		builder.addProbe(probeId, 1);
	}

	@Override
	public void visitInsnWithProbe(final int opcode, final int probeId) {
		String sign = opcode + separator + currentNo;
		builder.addInstruction(currentNode, sign, probeId);
		builder.addProbe(probeId, 0);
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
		String label1 = getLableString(dflt);
		for (final Label l : labels) {
			label1 += separator + getLableString(l);
		}
		String sign = label1 + separator + currentNo;
		builder.addInstruction(currentNode, sign, currentProbeId);
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		int branch = 0;
		visitSwitchTarget(dflt, branch);
		for (final Label l : labels) {
			branch++;
			visitSwitchTarget(l, branch);
		}
	}

	private void visitSwitchTarget(final Label label, final int branch) {
		final int id = LabelInfo.getProbeId(label);
		if (!LabelInfo.isDone(label)) {
			if (id == LabelInfo.NO_PROBE) {
				builder.addJump(label, branch);
			} else {
				builder.addProbe(id, branch);
			}
			LabelInfo.setDone(label);
		}
	}

}
