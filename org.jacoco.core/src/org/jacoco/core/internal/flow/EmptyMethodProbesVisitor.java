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
package org.jacoco.core.internal.flow;

import org.objectweb.asm.Label;

/**
 * This method probes visitor does nothing.
 */
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

	/**
	 * @return the only instance of this class
	 */
	public static MethodProbesVisitor getInstance() {
		return INSTANCE;
	}
}
