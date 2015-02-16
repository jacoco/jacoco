/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.JaCoCo;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

/**
 * Adapter that creates additional visitor events for probes to be inserted into
 * a method.
 */
public final class MethodProbesAdapter extends MethodVisitor {

	private final MethodProbesVisitor probesVisitor;

	private final IProbeIdGenerator idGenerator;

	private AnalyzerAdapter analyzer;

	private final Map<Label, Label> tryCatchProbeLabels;

	/**
	 * Create a new adapter instance.
	 * 
	 * @param probesVisitor
	 *            visitor to delegate to
	 * @param idGenerator
	 *            generator for unique probe ids
	 */
	public MethodProbesAdapter(final MethodProbesVisitor probesVisitor,
			final IProbeIdGenerator idGenerator) {
		super(JaCoCo.ASM_API_VERSION, probesVisitor);
		this.probesVisitor = probesVisitor;
		this.idGenerator = idGenerator;
		this.tryCatchProbeLabels = new HashMap<Label, Label>();
	}

	/**
	 * If an analyzer is set {@link IFrame} handles are calculated and emitted
	 * to the probes methods.
	 * 
	 * @param analyzer
	 *            optional analyzer to set
	 */
	public void setAnalyzer(final AnalyzerAdapter analyzer) {
		this.analyzer = analyzer;
	}

	@Override
	public void visitTryCatchBlock(Label start, final Label end,
			final Label handler, final String type) {
		// If a probe will be inserted before the start label, we'll need to use
		// a different label for the try-catch block.
		if (tryCatchProbeLabels.containsKey(start)) {
			start = tryCatchProbeLabels.get(start);
		} else if (LabelInfo.needsProbe(start)) {
			final Label probeLabel = new Label();
			LabelInfo.setSuccessor(probeLabel);
			tryCatchProbeLabels.put(start, probeLabel);
			start = probeLabel;
		}
		probesVisitor.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitLabel(final Label label) {
		if (LabelInfo.needsProbe(label)) {
			if (tryCatchProbeLabels.containsKey(label)) {
				probesVisitor.visitLabel(tryCatchProbeLabels.get(label));
			}
			probesVisitor.visitProbe(idGenerator.nextId());
		}
		probesVisitor.visitLabel(label);
	}

	@Override
	public void visitInsn(final int opcode) {
		switch (opcode) {
		case Opcodes.IRETURN:
		case Opcodes.LRETURN:
		case Opcodes.FRETURN:
		case Opcodes.DRETURN:
		case Opcodes.ARETURN:
		case Opcodes.RETURN:
		case Opcodes.ATHROW:
			probesVisitor.visitInsnWithProbe(opcode, idGenerator.nextId());
			break;
		default:
			probesVisitor.visitInsn(opcode);
			break;
		}
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		if (LabelInfo.isMultiTarget(label)) {
			probesVisitor.visitJumpInsnWithProbe(opcode, label,
					idGenerator.nextId(), frame(jumpPopCount(opcode)));
		} else {
			probesVisitor.visitJumpInsn(opcode, label);
		}
	}

	private int jumpPopCount(final int opcode) {
		switch (opcode) {
		case Opcodes.GOTO:
			return 0;
		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFLE:
		case Opcodes.IFNULL:
		case Opcodes.IFNONNULL:
			return 1;
		default: // IF_CMPxx and IF_ACMPxx
			return 2;
		}
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		if (markLabels(dflt, labels)) {
			probesVisitor.visitLookupSwitchInsnWithProbes(dflt, keys, labels,
					frame(1));
		} else {
			probesVisitor.visitLookupSwitchInsn(dflt, keys, labels);
		}
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label... labels) {
		if (markLabels(dflt, labels)) {
			probesVisitor.visitTableSwitchInsnWithProbes(min, max, dflt,
					labels, frame(1));
		} else {
			probesVisitor.visitTableSwitchInsn(min, max, dflt, labels);
		}
	}

	private boolean markLabels(final Label dflt, final Label[] labels) {
		boolean probe = false;
		LabelInfo.resetDone(labels);
		if (LabelInfo.isMultiTarget(dflt)) {
			LabelInfo.setProbeId(dflt, idGenerator.nextId());
			probe = true;
		}
		LabelInfo.setDone(dflt);
		for (final Label l : labels) {
			if (LabelInfo.isMultiTarget(l) && !LabelInfo.isDone(l)) {
				LabelInfo.setProbeId(l, idGenerator.nextId());
				probe = true;
			}
			LabelInfo.setDone(l);
		}
		return probe;
	}

	private IFrame frame(final int popCount) {
		return FrameSnapshot.create(analyzer, popCount);
	}

}
