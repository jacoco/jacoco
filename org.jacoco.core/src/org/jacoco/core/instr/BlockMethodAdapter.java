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
package org.jacoco.core.instr;

import org.jacoco.core.internal.flow.IProbeIdGenerator;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A method visitor that determines block boundaries and reports them to the
 * wrapped visitor. The implementation first buffers the content of the method
 * to extract all control flow target labels. At the end of the method it
 * flushes the content to the next visitor.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
final class BlockMethodAdapter extends MethodNode {

	private final IBlockMethodVisitor blockVisitor;

	private final IProbeIdGenerator idGenerator;

	/**
	 * Create a new adapter for the given block visitor.
	 * 
	 * @param blockVisitor
	 *            visitor to report block boundaries to
	 * @param idGenerator
	 *            generator for probe ids
	 * @param access
	 *            the method's access flags
	 * @param name
	 *            the method's name.
	 * @param desc
	 *            the method's descriptor
	 * @param signature
	 *            the method's signature. May be <tt>null</tt>.
	 * @param exceptions
	 *            the internal names of the method's exception classes. May be
	 *            <tt>null</tt>.
	 */
	public BlockMethodAdapter(final IBlockMethodVisitor blockVisitor,
			final IProbeIdGenerator idGenerator, final int access,
			final String name, final String desc, final String signature,
			final String[] exceptions) {
		super(access, name, desc, signature, exceptions);
		this.blockVisitor = blockVisitor;
		this.idGenerator = idGenerator;
	}

	// === MethodVisitor ===

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		super.visitJumpInsn(opcode, label);
		setTarget(label);
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		super.visitTableSwitchInsn(min, max, dflt, labels);
		setTarget(dflt);
		for (final Label l : labels) {
			setTarget(l);
		}
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		super.visitLookupSwitchInsn(dflt, keys, labels);
		setTarget(dflt);
		for (final Label l : labels) {
			setTarget(l);
		}
	}

	@Override
	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		super.visitTryCatchBlock(start, end, handler, type);
		setTarget(start);
		setTarget(end);
		setTarget(handler);
	}

	@Override
	public void visitEnd() {
		accept(new BlockFinder());
	}

	private final class BlockFinder extends MethodAdapter {

		private boolean blockStarted;

		private int id;

		public BlockFinder() {
			super(blockVisitor);
			blockStarted = false;
		}

		private void onBlockEndBeforeJump() {
			if (blockStarted) {
				id = idGenerator.nextId();
				blockVisitor.visitBlockEndBeforeJump(id);
			}
		}

		private void onBlockEnd() {
			if (blockStarted) {
				blockVisitor.visitBlockEnd(id);
				blockStarted = false;
			}
		}

		@Override
		public void visitLabel(final Label label) {
			if (isTarget(label)) {
				onBlockEndBeforeJump();
				onBlockEnd();
			}
			super.visitLabel(label);
		}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {
			blockStarted = true;
			onBlockEndBeforeJump();
			super.visitJumpInsn(opcode, label);
			onBlockEnd();
		}

		@Override
		public void visitInsn(final int opcode) {
			blockStarted = true;
			switch (opcode) {
			case Opcodes.RETURN:
			case Opcodes.IRETURN:
			case Opcodes.FRETURN:
			case Opcodes.LRETURN:
			case Opcodes.DRETURN:
			case Opcodes.ARETURN:
			case Opcodes.ATHROW:
				onBlockEndBeforeJump();
				super.visitInsn(opcode);
				onBlockEnd();
				break;
			default:
				super.visitInsn(opcode);
				break;
			}
		}

		@Override
		public void visitTableSwitchInsn(final int min, final int max,
				final Label dflt, final Label[] labels) {
			blockStarted = true;
			onBlockEndBeforeJump();
			super.visitTableSwitchInsn(min, max, dflt, labels);
			onBlockEnd();
		}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
				final Label[] labels) {
			blockStarted = true;
			onBlockEndBeforeJump();
			super.visitLookupSwitchInsn(dflt, keys, labels);
			onBlockEnd();
		}

		@Override
		public void visitFieldInsn(final int opcode, final String owner,
				final String name, final String desc) {
			blockStarted = true;
			super.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitIincInsn(final int var, final int increment) {
			blockStarted = true;
			super.visitIincInsn(var, increment);
		}

		@Override
		public void visitIntInsn(final int opcode, final int operand) {
			blockStarted = true;
			super.visitIntInsn(opcode, operand);
		}

		@Override
		public void visitLdcInsn(final Object cst) {
			blockStarted = true;
			super.visitLdcInsn(cst);
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner,
				final String name, final String desc) {
			blockStarted = true;
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitMultiANewArrayInsn(final String desc, final int dims) {
			blockStarted = true;
			super.visitMultiANewArrayInsn(desc, dims);
		}

		@Override
		public void visitTypeInsn(final int opcode, final String type) {
			blockStarted = true;
			super.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitVarInsn(final int opcode, final int var) {
			blockStarted = true;
			super.visitVarInsn(opcode, var);
		}

	}

	// === Utilities for marking jump target labels: ===

	private static final class TargetMarker {

		final LabelNode labelNode;

		TargetMarker(final LabelNode labelNode) {
			this.labelNode = labelNode;
		}

	}

	@Override
	protected LabelNode getLabelNode(final Label l) {
		if (l.info instanceof TargetMarker) {
			return ((TargetMarker) l.info).labelNode;
		}
		return super.getLabelNode(l);
	}

	private final void setTarget(final Label l) {
		if (!isTarget(l)) {
			l.info = new TargetMarker(getLabelNode(l));
		}
	}

	private final boolean isTarget(final Label l) {
		return l.info instanceof TargetMarker;
	}

}
