/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hare Robertson - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis.filters;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jacoco.core.analysis.filters.ICoverageFilterStatus.ICoverageFilter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Filter that disabled coverage tracking of the exception exit branch from
 * synchronized blocks
 */
public class SynchronizedExitCoverageFilter implements ICoverageFilter {

	private static enum State {
		START, HANDLER, MONITEREXIT;
	}

	private final LinkedList<Boolean> handlerEnabled = new LinkedList<Boolean>();
	private boolean enabled = true;
	private State syncExitState = State.START;

	public boolean enabled() {
		return enabled;
	}

	public boolean includeClass(final String className) {
		return true;
	}

	public ClassVisitor visitClass(final ClassVisitor delegate) {
		return delegate;
	}

	public MethodVisitor preVisitMethod(final String name, final String desc,
			final MethodVisitor delegate) {
		enabled = true;
		handlerEnabled.clear();
		return new SyncAnalyzerVisitor(delegate);
	}

	private class SyncAnalyzerVisitor extends MethodVisitor {
		private final Set<Label> handlers = new HashSet<Label>();

		private SyncAnalyzerVisitor(final MethodVisitor delegate) {
			super(Opcodes.ASM4, delegate);
		}

		@Override
		public void visitTryCatchBlock(final Label start, final Label end,
				final Label handler, final String type) {
			if (type == null) {
				handlers.add(handler);
			}
			syncExitState = State.START;
			super.visitTryCatchBlock(start, end, handler, type);
		}

		@Override
		public void visitLabel(final Label label) {
			if ((syncExitState == State.START) && handlers.contains(label)) {
				handlerEnabled.addLast(Boolean.TRUE);
				syncExitState = State.HANDLER;
			}
			super.visitLabel(label);
		}

		@Override
		public void visitVarInsn(final int opcode, final int var) {
			if (((syncExitState == State.HANDLER) || (syncExitState == State.MONITEREXIT))
					&& ((opcode == Opcodes.ALOAD) || (opcode == Opcodes.ASTORE))) {
				// Ignore
			} else {
				syncExitState = State.START;
			}
			super.visitVarInsn(opcode, var);
		}

		@Override
		public void visitInsn(final int opcode) {
			if ((syncExitState == State.HANDLER)
					&& Opcodes.MONITOREXIT == opcode) {
				syncExitState = State.MONITEREXIT;
			} else if ((syncExitState == State.MONITEREXIT)
					&& Opcodes.ATHROW == opcode) {
				handlerEnabled.removeLast();
				handlerEnabled.addLast(Boolean.FALSE);
				syncExitState = State.START;
			}
			super.visitInsn(opcode);
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner,
				final String name, final String desc) {
			syncExitState = State.START;
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		public AnnotationVisitor visitAnnotation(final String desc,
				final boolean visible) {
			syncExitState = State.START;
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			syncExitState = State.START;
			return super.visitAnnotationDefault();
		}

		@Override
		public void visitAttribute(final Attribute attr) {
			syncExitState = State.START;
			super.visitAttribute(attr);
		}

		@Override
		public void visitCode() {
			// syncExitState = State.START;
			super.visitCode();
		}

		@Override
		public void visitEnd() {
			// syncExitState = State.START;
			super.visitEnd();
		}

		@Override
		public void visitFieldInsn(final int opcode, final String owner,
				final String name, final String desc) {
			syncExitState = State.START;
			super.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitFrame(final int type, final int nLocal,
				final Object[] local, final int nStack, final Object[] stack) {
			// syncExitState = State.START;
			super.visitFrame(type, nLocal, local, nStack, stack);
		}

		@Override
		public void visitIincInsn(final int var, final int increment) {
			syncExitState = State.START;
			super.visitIincInsn(var, increment);
		}

		@Override
		public void visitIntInsn(final int opcode, final int operand) {
			syncExitState = State.START;
			super.visitIntInsn(opcode, operand);
		}

		@Override
		public void visitInvokeDynamicInsn(final String name,
				final String desc, final Handle bsm, final Object... bsmArgs) {
			syncExitState = State.START;
			super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {
			syncExitState = State.START;
			super.visitJumpInsn(opcode, label);
		}

		@Override
		public void visitLdcInsn(final Object cst) {
			syncExitState = State.START;
			super.visitLdcInsn(cst);
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {
			// syncExitState = State.START;
			super.visitLineNumber(line, start);
		}

		@Override
		public void visitLocalVariable(final String name, final String desc,
				final String signature, final Label start, final Label end,
				final int index) {
			// syncExitState = State.START;
			super.visitLocalVariable(name, desc, signature, start, end, index);
		}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
				final Label[] labels) {
			syncExitState = State.START;
			super.visitLookupSwitchInsn(dflt, keys, labels);
		}

		@Override
		public void visitMaxs(final int maxStack, final int maxLocals) {
			// syncExitState = State.START;
			super.visitMaxs(maxStack, maxLocals);
		}

		@Override
		public void visitMultiANewArrayInsn(final String desc, final int dims) {
			syncExitState = State.START;
			super.visitMultiANewArrayInsn(desc, dims);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(final int parameter,
				final String desc, final boolean visible) {
			syncExitState = State.START;
			return super.visitParameterAnnotation(parameter, desc, visible);
		}

		@Override
		public void visitTableSwitchInsn(final int min, final int max,
				final Label dflt, final Label... labels) {
			syncExitState = State.START;
			super.visitTableSwitchInsn(min, max, dflt, labels);
		}

		@Override
		public void visitTypeInsn(final int opcode, final String type) {
			syncExitState = State.START;
			super.visitTypeInsn(opcode, type);
		}
	}

	public MethodProbesVisitor visitMethod(final String name,
			final String desc, final MethodProbesVisitor delegate) {
		return new SyncFilterVisitor(delegate);
	}

	private class SyncFilterVisitor extends MethodProbesVisitor {
		private final Set<Label> handlers = new HashSet<Label>();
		private final MethodProbesVisitor delegate;

		private SyncFilterVisitor(final MethodProbesVisitor delegate) {
			super(delegate);
			this.delegate = delegate;
		}

		@Override
		public void visitTryCatchBlock(final Label start, final Label end,
				final Label handler, final String type) {
			if (type == null) {
				handlers.add(handler);
			}
			super.visitTryCatchBlock(start, end, handler, type);
		}

		@Override
		public void visitLabel(final Label label) {
			if (handlers.contains(label)) {
				enabled = Boolean.TRUE.equals(handlerEnabled.removeFirst());
			}
			super.visitLabel(label);
		}

		@Override
		public void visitInsn(final int opcode) {
			if (!enabled && (opcode == Opcodes.ATHROW)) {
				enabled = true;
			}
			super.visitInsn(opcode);
		}

		// --- Simple methods that pass on to the delegate ---

		@Override
		public void visitProbe(final int probeId) {
			delegate.visitProbe(probeId);
		}

		@Override
		public void visitJumpInsnWithProbe(final int opcode, final Label label,
				final int probeId) {
			delegate.visitJumpInsnWithProbe(opcode, label, probeId);
		}

		@Override
		public void visitInsnWithProbe(final int opcode, final int probeId) {
			delegate.visitInsnWithProbe(opcode, probeId);
			if (!enabled && (opcode == Opcodes.ATHROW)) {
				enabled = true;
			}
		}

		@Override
		public void visitTableSwitchInsnWithProbes(final int min,
				final int max, final Label dflt, final Label[] labels) {
			delegate.visitTableSwitchInsnWithProbes(min, max, dflt, labels);
		}

		@Override
		public void visitLookupSwitchInsnWithProbes(final Label dflt,
				final int[] keys, final Label[] labels) {
			delegate.visitLookupSwitchInsnWithProbes(dflt, keys, labels);
		}
	}

}
