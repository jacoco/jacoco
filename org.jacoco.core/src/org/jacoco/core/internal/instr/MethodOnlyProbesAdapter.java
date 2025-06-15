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
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A simplified MethodProbesVisitor for method-only coverage mode.
 * This adapter only inserts a single probe at the beginning of each method
 * and ignores all other probe insertion points.
 */
class MethodOnlyProbesAdapter extends MethodProbesVisitor {

	private final IProbeInserter probeInserter;
	private boolean firstProbe = true;

	/**
	 * Create a new adapter for method-only coverage.
	 * 
	 * @param mv
	 *            next method visitor in the chain
	 * @param probeInserter
	 *            call-back to insert probes
	 */
	public MethodOnlyProbesAdapter(final MethodVisitor mv,
			final IProbeInserter probeInserter) {
		super(mv);
		this.probeInserter = probeInserter;
	}

	// Override probe insertion methods to do nothing except for the first probe

	@Override
	public void visitProbe(final int probeId) {
		// In method-only mode, insert only the first probe we encounter
		// This will be at method entry
		if (firstProbe) {
			probeInserter.insertProbe(probeId);
			firstProbe = false;
		}
	}

	@Override
	public void visitInsnWithProbe(final int opcode, final int probeId) {
		// Just visit the instruction without inserting a probe
		mv.visitInsn(opcode);
	}

	@Override
	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId, final IFrame frame) {
		// Just visit the jump instruction without inserting a probe
		mv.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels, final IFrame frame) {
		// Just visit the switch instruction without inserting probes
		mv.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels, final IFrame frame) {
		// Just visit the lookup switch without inserting probes
		mv.visitLookupSwitchInsn(dflt, keys, labels);
	}
}