/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Internal wrapper for the FrameTracker which activates frame tracking lazily
 * when the first frame is reported.
 */
class LazyFrameTracker extends MethodVisitor implements IFrameInserter {

	private final String owner;
	private FrameTracker tracker;

	public LazyFrameTracker(final MethodVisitor mv, final String owner) {
		super(Opcodes.ASM4, mv);
		this.owner = owner;
		this.tracker = null;
	}

	@Override
	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {
		if (tracker == null) {
			mv = tracker = new FrameTracker(mv, owner);
		}
		tracker.visitFrame(type, nLocal, local, nStack, stack);
	}

	public void insertFrame() {
		if (tracker != null) {
			tracker.insertFrame();
		}
	}

}
