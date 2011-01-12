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
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.flow.IMethodProbesVisitor;
import org.jacoco.core.internal.flow.LabelInfo;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This method adapter inserts probes as requested by the
 * {@link IMethodProbesVisitor} events.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
class MethodInstrumenter extends ProbeVariableInserter implements
		IMethodProbesVisitor {

	private final IProbeArrayStrategy probeArrayStrategy;

	private int accessorStackSize;

	/**
	 * Create a new instrumenter instance for the given method.
	 * 
	 * @param mv
	 *            next method visitor in the chain
	 * @param access
	 *            access flags for the method
	 * @param desc
	 *            description of the method
	 * @param probeArrayStrategy
	 *            strategy to get access to the probe array
	 */
	public MethodInstrumenter(final MethodVisitor mv, final int access,
			final String desc, final IProbeArrayStrategy probeArrayStrategy) {
		super(access, desc, mv);
		this.probeArrayStrategy = probeArrayStrategy;
	}

	@Override
	public void visitCode() {
		super.visitCode();
		// At the very beginning of the method we load the boolean[] array into
		// a local variable that stores the probes for this class.
		accessorStackSize = probeArrayStrategy.pushInstance(mv);

		// Stack[0]: [Z

		mv.visitVarInsn(Opcodes.ASTORE, variable);
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		// Max stack size of the probe code is 3 which can add to the
		// original stack size depending on the probe locations. The accessor
		// stack size is an absolute maximum, as the accessor code is inserted
		// at the very beginning of each method when the stack size is empty.
		final int increasedStack = Math.max(maxStack + 3, accessorStackSize);
		super.visitMaxs(increasedStack, maxLocals);
	}

	private void insertProbe(final int id) {
		// At the end of every block we set the corresponding position in the
		// boolean[] array to true.

		mv.visitVarInsn(Opcodes.ALOAD, variable);

		// Stack[0]: [Z

		InstrSupport.push(mv, id);

		// Stack[1]: I
		// Stack[0]: [Z

		mv.visitInsn(Opcodes.ICONST_1);

		// Stack[2]: I
		// Stack[1]: I
		// Stack[0]: [Z

		visitInsn(Opcodes.BASTORE);
	}

	// === IMethodProbesVisitor ===

	public void visitProbe(final int probeId) {
		insertProbe(probeId);
	}

	public void visitInsnWithProbe(final int opcode, final int probeId) {
		insertProbe(probeId);
		mv.visitInsn(opcode);
	}

	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId) {
		if (opcode == Opcodes.GOTO) {
			insertProbe(probeId);
			mv.visitJumpInsn(Opcodes.GOTO, label);
		} else {
			final Label l = new Label();
			mv.visitJumpInsn(getNegation(opcode), l);
			insertProbe(probeId);
			mv.visitJumpInsn(Opcodes.GOTO, label);
			mv.visitLabel(l);
		}
	}

	private int getNegation(final int opcode) {
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
		throw new AssertionError(opcode);
	}

	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels) {
		// 1. Calculate intermediate labels:
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		final Label newDflt = createIntermediate(dflt);
		final Label[] newLabels = createIntermediates(labels);
		mv.visitTableSwitchInsn(min, max, newDflt, newLabels);

		// 2. Insert probes:
		insertIntermediateProbes(dflt, labels);
	}

	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels) {
		// 1. Calculate intermediate labels:
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		final Label newDflt = createIntermediate(dflt);
		final Label[] newLabels = createIntermediates(labels);
		mv.visitLookupSwitchInsn(newDflt, keys, newLabels);

		// 2. Insert probes:
		insertIntermediateProbes(dflt, labels);
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

	private Label[] createIntermediates(final Label[] labels) {
		final Label[] intermediates = new Label[labels.length];
		for (int i = 0; i < labels.length; i++) {
			intermediates[i] = createIntermediate(labels[i]);
		}
		return intermediates;
	}

	private void insertIntermediateProbe(final Label label) {
		final int probeId = LabelInfo.getProbeId(label);
		if (probeId != LabelInfo.NO_PROBE && !LabelInfo.isDone(label)) {
			mv.visitLabel(LabelInfo.getIntermediateLabel(label));
			insertProbe(probeId);
			mv.visitJumpInsn(Opcodes.GOTO, label);
			LabelInfo.setDone(label);
		}
	}

	private void insertIntermediateProbes(final Label dflt, final Label[] labels) {
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		insertIntermediateProbe(dflt);
		for (final Label l : labels) {
			insertIntermediateProbe(l);
		}
	}

}
