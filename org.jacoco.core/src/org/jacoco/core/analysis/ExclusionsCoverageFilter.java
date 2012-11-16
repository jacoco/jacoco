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
package org.jacoco.core.analysis;

import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

/**
 * {@link ICoverageFilter} which searches for instances of:
 * 
 * <pre>
 * Runtime.getRuntime().equals(&quot;jacoco.off&quot;);
 * </pre>
 * 
 * or
 * 
 * <pre>
 * Runtime.getRuntime().equals(&quot;jacoco.on&quot;);
 * </pre>
 */
public class ExclusionsCoverageFilter implements ICoverageFilter {

	boolean pendingCoverageEnabled = true;
	boolean coverageEnabled = true;

	private enum State {
		SEARCHING, GET_RUNTIME, LOAD_CONSTANT, EQUALS;
	}

	private State state = State.SEARCHING;

	public boolean includeClass(final String className) {
		// About to process a new class - reset our state
		pendingCoverageEnabled = true;
		coverageEnabled = true;
		return true;
	}

	public boolean enabled() {
		return coverageEnabled;
	}

	public MethodProbesVisitor getVisitor(final MethodProbesVisitor delegate) {
		return new ExclusionsCoverageVisitor(delegate);
	}

	private class ExclusionsCoverageVisitor extends MethodProbesVisitor {

		private final MethodProbesVisitor delegate;

		private ExclusionsCoverageVisitor(final MethodProbesVisitor delegate) {
			super(delegate);
			this.delegate = delegate;
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner,
				final String name, final String desc) {

			if ((state == State.SEARCHING) && (opcode == Opcodes.INVOKESTATIC)
					&& ("java/lang/Runtime".equals(owner))
					&& ("getRuntime".equals(name))
					&& ("()Ljava/lang/Runtime;".equals(desc))) {
				state = State.GET_RUNTIME;
			} else if ((state == State.LOAD_CONSTANT)
					&& (opcode == Opcodes.INVOKEVIRTUAL)
					&& ("java/lang/Object".equals(owner))
					&& ("equals".equals(name))
					&& ("(Ljava/lang/Object;)Z".equals(desc))) {
				state = State.EQUALS;
			} else {
				state = State.SEARCHING;
			}
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitLdcInsn(final Object cst) {
			if ((state == State.GET_RUNTIME) && ("jacoco.off".equals(cst))) {
				pendingCoverageEnabled = false;
				state = State.LOAD_CONSTANT;
			} else if ((state == State.GET_RUNTIME)
					&& ("jacoco.on".equals(cst))) {
				pendingCoverageEnabled = true;
				state = State.LOAD_CONSTANT;
			} else {
				state = State.SEARCHING;
			}
			super.visitLdcInsn(cst);
		}

		@Override
		public void visitInsn(final int opcode) {
			if ((state == State.EQUALS) && (opcode == Opcodes.POP)) {
				coverageEnabled = pendingCoverageEnabled;
			}
			state = State.SEARCHING;
			super.visitInsn(opcode);
		}

		// Simple methods

		@Override
		public AnnotationVisitor visitAnnotation(final String desc,
				final boolean visible) {
			state = State.SEARCHING;
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			state = State.SEARCHING;
			return super.visitAnnotationDefault();
		}

		@Override
		public void visitAttribute(final Attribute attr) {
			state = State.SEARCHING;
			super.visitAttribute(attr);
		}

		@Override
		public void visitCode() {
			state = State.SEARCHING;
			super.visitCode();
		}

		@Override
		public void visitEnd() {
			state = State.SEARCHING;
			super.visitEnd();
		}

		@Override
		public void visitFieldInsn(final int opcode, final String owner,
				final String name, final String desc) {
			state = State.SEARCHING;
			super.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitFrame(final int type, final int nLocal,
				final Object[] local, final int nStack, final Object[] stack) {
			state = State.SEARCHING;
			super.visitFrame(type, nLocal, local, nStack, stack);
		}

		@Override
		public void visitIincInsn(final int var, final int increment) {
			state = State.SEARCHING;
			super.visitIincInsn(var, increment);
		}

		@Override
		public void visitIntInsn(final int opcode, final int operand) {
			state = State.SEARCHING;
			super.visitIntInsn(opcode, operand);
		}

		@Override
		public void visitInvokeDynamicInsn(final String name,
				final String desc, final Handle bsm, final Object... bsmArgs) {
			state = State.SEARCHING;
			super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {
			state = State.SEARCHING;
			super.visitJumpInsn(opcode, label);
		}

		@Override
		public void visitLabel(final Label label) {
			state = State.SEARCHING;
			super.visitLabel(label);
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {
			state = State.SEARCHING;
			super.visitLineNumber(line, start);
		}

		@Override
		public void visitLocalVariable(final String name, final String desc,
				final String signature, final Label start, final Label end,
				final int index) {
			state = State.SEARCHING;
			super.visitLocalVariable(name, desc, signature, start, end, index);
		}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
				final Label[] labels) {
			state = State.SEARCHING;
			super.visitLookupSwitchInsn(dflt, keys, labels);
		}

		@Override
		public void visitMaxs(final int maxStack, final int maxLocals) {
			state = State.SEARCHING;
			super.visitMaxs(maxStack, maxLocals);
		}

		@Override
		public void visitMultiANewArrayInsn(final String desc, final int dims) {
			state = State.SEARCHING;
			super.visitMultiANewArrayInsn(desc, dims);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(final int parameter,
				final String desc, final boolean visible) {
			state = State.SEARCHING;
			return super.visitParameterAnnotation(parameter, desc, visible);
		}

		@Override
		public void visitTableSwitchInsn(final int min, final int max,
				final Label dflt, final Label... labels) {
			state = State.SEARCHING;
			super.visitTableSwitchInsn(min, max, dflt, labels);
		}

		@Override
		public void visitTryCatchBlock(final Label start, final Label end,
				final Label handler, final String type) {
			state = State.SEARCHING;
			super.visitTryCatchBlock(start, end, handler, type);
		}

		@Override
		public void visitTypeInsn(final int opcode, final String type) {
			state = State.SEARCHING;
			super.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitVarInsn(final int opcode, final int var) {
			state = State.SEARCHING;
			super.visitVarInsn(opcode, var);
		}

		@Override
		public void visitProbe(final int probeId) {
			state = State.SEARCHING;
			delegate.visitProbe(probeId);
		}

		@Override
		public void visitJumpInsnWithProbe(final int opcode, final Label label,
				final int probeId) {
			state = State.SEARCHING;
			delegate.visitJumpInsnWithProbe(opcode, label, probeId);
		}

		@Override
		public void visitInsnWithProbe(final int opcode, final int probeId) {
			state = State.SEARCHING;
			delegate.visitInsnWithProbe(opcode, probeId);
		}

		@Override
		public void visitTableSwitchInsnWithProbes(final int min,
				final int max, final Label dflt, final Label[] labels) {
			state = State.SEARCHING;
			delegate.visitTableSwitchInsnWithProbes(min, max, dflt, labels);
		}

		@Override
		public void visitLookupSwitchInsnWithProbes(final Label dflt,
				final int[] keys, final Label[] labels) {
			state = State.SEARCHING;
			delegate.visitLookupSwitchInsnWithProbes(dflt, keys, labels);
		}
	}
}
