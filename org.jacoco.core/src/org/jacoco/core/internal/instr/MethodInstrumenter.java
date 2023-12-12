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
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.LabelInfo;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This method adapter inserts probes as requested by the
 * {@link MethodProbesVisitor} events.
 */
class MethodInstrumenter extends MethodProbesVisitor {

	private final IProbeInserter probeInserter;

	/**
	 * Create a new instrumenter instance for the given method.
	 *
	 * @param mv
	 *            next method visitor in the chain
	 * @param probeInserter
	 *            call-back to insert probes where required
	 */
	public MethodInstrumenter(final MethodVisitor mv,
			final IProbeInserter probeInserter) {
		super(mv);
		this.probeInserter = probeInserter;
	}

	// === IMethodProbesVisitor ===

	@Override
	public void visitProbe(final int probeId) {
		probeInserter.insertProbe(probeId);
	}

	@Override
	public void visitInsnWithProbe(final int opcode, final int probeId) {
		probeInserter.insertProbe(probeId);
		mv.visitInsn(opcode);
	}

	@Override
	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId, final IFrame frame) {
		if (opcode == Opcodes.GOTO) {
			probeInserter.insertProbe(probeId);
			mv.visitJumpInsn(Opcodes.GOTO, label);
		} else {
			final Label intermediate = new Label();
			mv.visitJumpInsn(getInverted(opcode), intermediate);
			probeInserter.insertProbe(probeId);
			mv.visitJumpInsn(Opcodes.GOTO, label);
			mv.visitLabel(intermediate);
			frame.accept(mv);
		}
	}

	private int getInverted(final int opcode) {
		switch (opcode) {
		case Opcodes.IFEQ:
			return Opcodes.IFNE;
		case Opcodes.IFNE:
			return Opcodes.IFEQ;
		case Opcodes.IFLT:
			return Opcodes.IFGE;
		case Opcodes.IFGE:
			return Opcodes.IFLT;
		case Opcodes.IFGT:
			return Opcodes.IFLE;
		case Opcodes.IFLE:
			return Opcodes.IFGT;
		case Opcodes.IF_ICMPEQ:
			return Opcodes.IF_ICMPNE;
		case Opcodes.IF_ICMPNE:
			return Opcodes.IF_ICMPEQ;
		case Opcodes.IF_ICMPLT:
			return Opcodes.IF_ICMPGE;
		case Opcodes.IF_ICMPGE:
			return Opcodes.IF_ICMPLT;
		case Opcodes.IF_ICMPGT:
			return Opcodes.IF_ICMPLE;
		case Opcodes.IF_ICMPLE:
			return Opcodes.IF_ICMPGT;
		case Opcodes.IF_ACMPEQ:
			return Opcodes.IF_ACMPNE;
		case Opcodes.IF_ACMPNE:
			return Opcodes.IF_ACMPEQ;
		case Opcodes.IFNULL:
			return Opcodes.IFNONNULL;
		case Opcodes.IFNONNULL:
			return Opcodes.IFNULL;
		}
		throw new IllegalArgumentException();
	}

	@Override
	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels, final IFrame frame) {
		// 1. Calculate intermediate labels:
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		final Label newDflt = createIntermediate(dflt);
		final Label[] newLabels = createIntermediates(labels);
		mv.visitTableSwitchInsn(min, max, newDflt, newLabels);

		// 2. Insert probes:
		insertIntermediateProbes(dflt, labels, frame);
	}

	@Override
	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels, final IFrame frame) {
		// 1. Calculate intermediate labels:
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		final Label newDflt = createIntermediate(dflt);
		final Label[] newLabels = createIntermediates(labels);
		mv.visitLookupSwitchInsn(newDflt, keys, newLabels);

		// 2. Insert probes:
		insertIntermediateProbes(dflt, labels, frame);
	}

	private Label[] createIntermediates(final Label[] labels) {
		final Label[] intermediates = new Label[labels.length];
		for (int i = 0; i < labels.length; i++) {
			intermediates[i] = createIntermediate(labels[i]);
		}
		return intermediates;
	}

	private Label createIntermediate(final Label label) {
		final Label intermediate;
		if (LabelInfo.getProbeId(label) == LabelInfo.NO_PROBE) {
			intermediate = label;
		} else {
			if (LabelInfo.isDone(label)) {
				intermediate = LabelInfo.getIntermediateLabel(label);
			} else {
				intermediate = new Label();
				LabelInfo.setIntermediateLabel(label, intermediate);
				LabelInfo.setDone(label);
			}
		}
		return intermediate;
	}

	private void insertIntermediateProbe(final Label label,
			final IFrame frame) {
		final int probeId = LabelInfo.getProbeId(label);
		if (probeId != LabelInfo.NO_PROBE && !LabelInfo.isDone(label)) {
			mv.visitLabel(LabelInfo.getIntermediateLabel(label));
			frame.accept(mv);
			probeInserter.insertProbe(probeId);
			mv.visitJumpInsn(Opcodes.GOTO, label);
			LabelInfo.setDone(label);
		}
	}

	private void insertIntermediateProbes(final Label dflt,
			final Label[] labels, final IFrame frame) {
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		insertIntermediateProbe(dflt, frame);
		for (final Label l : labels) {
			insertIntermediateProbe(l, frame);
		}
	}

}
