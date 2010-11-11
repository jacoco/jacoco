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
package org.jacoco.core.internal.flow;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.Opcodes;

/**
 * Adapter that creates additional visitor events for probes to be inserted into
 * a method.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
final class MethodProbesAdapter extends MethodAdapter {

	private final IMethodProbesVisitor probesVisitor;

	private final IProbeIdGenerator idGenerator;

	public MethodProbesAdapter(final IMethodProbesVisitor probesVisitor,
			final IProbeIdGenerator idGenerator) {
		super(probesVisitor);
		this.probesVisitor = probesVisitor;
		this.idGenerator = idGenerator;
	}

	@Override
	public void visitLabel(final Label label) {
		if (LabelsInfo.isMultiTarget(label) && LabelsInfo.isSuccessor(label)) {
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
		if (LabelsInfo.isMultiTarget(label)) {
			probesVisitor.visitJumpInsnWithProbe(opcode, label,
					idGenerator.nextId());
		} else {
			probesVisitor.visitJumpInsn(opcode, label);
		}
	}

}
