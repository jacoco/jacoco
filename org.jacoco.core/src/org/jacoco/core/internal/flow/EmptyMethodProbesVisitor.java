package org.jacoco.core.internal.flow;

import org.objectweb.asm.Label;

public class EmptyMethodProbesVisitor extends MethodProbesVisitor {
	private static final MethodProbesVisitor INSTANCE = new EmptyMethodProbesVisitor();

	@Override
	public void visitProbe(final int probeId) {
		// nothing to do
	}

	@Override
	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId, final IFrame frame) {
		// nothing to do
	}

	@Override
	public void visitInsnWithProbe(final int opcode, final int probeId) {
		// nothing to do
	}

	@Override
	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels, final IFrame frame) {
		// nothing to do
	}

	@Override
	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels, final IFrame frame) {
		// nothing to do
	}

	public static MethodProbesVisitor getInstance() {
		return INSTANCE;
	}
}
