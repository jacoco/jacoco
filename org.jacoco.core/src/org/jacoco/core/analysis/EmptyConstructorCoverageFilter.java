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

import org.jacoco.core.analysis.ICoverageFilterStatus.ICoverageFilter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Disable coverage of empty constructors
 */
public class EmptyConstructorCoverageFilter implements ICoverageFilter {

	private static enum State {
		START, ALOAD0, SUPER, EMPTY, NOT_EMPTY;
	}

	private boolean enabled = true;
	private State methodState = State.START;

	public boolean enabled() {
		return enabled;
	}

	public boolean includeClass(final String className) {
		return true;
	}

	public ClassVisitor visitClass(final ClassVisitor delegate) {
		return delegate;
	}

	public MethodVisitor preVisitMethod(final String name,
			final String signature, final MethodVisitor delegate) {
		methodState = State.START;
		if ("<init>".equals(name) && "()V".equals(signature)) {
			return new EmptyMethodVisitor(delegate);
		} else {
			return delegate;
		}
	}

	private class EmptyMethodVisitor extends MethodVisitor {
		private EmptyMethodVisitor(final MethodVisitor delegate) {
			super(Opcodes.ASM4, delegate);
		}

		@Override
		public void visitVarInsn(final int opcode, final int var) {
			if ((methodState == State.START) && (opcode == Opcodes.ALOAD)
					&& (var == 0)) {
				methodState = State.ALOAD0;
			} else {
				methodState = State.NOT_EMPTY;
			}
			super.visitVarInsn(opcode, var);
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner,
				final String name, final String desc) {
			if ((methodState == State.ALOAD0)
					&& (opcode == Opcodes.INVOKESPECIAL)
					&& ("<init>".equals(name)) && ("()V".equals(desc))) {
				methodState = State.SUPER;
			} else {
				methodState = State.NOT_EMPTY;
			}
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitInsn(final int opcode) {
			if ((methodState == State.SUPER) && Opcodes.RETURN == opcode) {
				methodState = State.EMPTY;
			} else {
				methodState = State.NOT_EMPTY;
			}
			super.visitInsn(opcode);
		}

		@Override
		public AnnotationVisitor visitAnnotation(final String desc,
				final boolean visible) {
			methodState = State.NOT_EMPTY;
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			methodState = State.NOT_EMPTY;
			return super.visitAnnotationDefault();
		}

		@Override
		public void visitAttribute(final Attribute attr) {
			methodState = State.NOT_EMPTY;
			super.visitAttribute(attr);
		}

		@Override
		public void visitCode() {
			methodState = State.NOT_EMPTY;
			super.visitCode();
		}

		@Override
		public void visitEnd() {
			methodState = State.NOT_EMPTY;
			super.visitEnd();
		}

		@Override
		public void visitFieldInsn(final int opcode, final String owner,
				final String name, final String desc) {
			methodState = State.NOT_EMPTY;
			super.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitFrame(final int type, final int nLocal,
				final Object[] local, final int nStack, final Object[] stack) {
			methodState = State.NOT_EMPTY;
			super.visitFrame(type, nLocal, local, nStack, stack);
		}

		@Override
		public void visitIincInsn(final int var, final int increment) {
			methodState = State.NOT_EMPTY;
			super.visitIincInsn(var, increment);
		}

		@Override
		public void visitIntInsn(final int opcode, final int operand) {
			methodState = State.NOT_EMPTY;
			super.visitIntInsn(opcode, operand);
		}

		@Override
		public void visitInvokeDynamicInsn(final String name,
				final String desc, final Handle bsm, final Object... bsmArgs) {
			methodState = State.NOT_EMPTY;
			super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {
			methodState = State.NOT_EMPTY;
			super.visitJumpInsn(opcode, label);
		}

		@Override
		public void visitLabel(final Label label) {
			methodState = State.NOT_EMPTY;
			super.visitLabel(label);
		}

		@Override
		public void visitLdcInsn(final Object cst) {
			methodState = State.NOT_EMPTY;
			super.visitLdcInsn(cst);
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {
			methodState = State.NOT_EMPTY;
			super.visitLineNumber(line, start);
		}

		@Override
		public void visitLocalVariable(final String name, final String desc,
				final String signature, final Label start, final Label end,
				final int index) {
			methodState = State.NOT_EMPTY;
			super.visitLocalVariable(name, desc, signature, start, end, index);
		}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
				final Label[] labels) {
			methodState = State.NOT_EMPTY;
			super.visitLookupSwitchInsn(dflt, keys, labels);
		}

		@Override
		public void visitMaxs(final int maxStack, final int maxLocals) {
			methodState = State.NOT_EMPTY;
			super.visitMaxs(maxStack, maxLocals);
		}

		@Override
		public void visitMultiANewArrayInsn(final String desc, final int dims) {
			methodState = State.NOT_EMPTY;
			super.visitMultiANewArrayInsn(desc, dims);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(final int parameter,
				final String desc, final boolean visible) {
			methodState = State.NOT_EMPTY;
			return super.visitParameterAnnotation(parameter, desc, visible);
		}

		@Override
		public void visitTableSwitchInsn(final int min, final int max,
				final Label dflt, final Label... labels) {
			methodState = State.NOT_EMPTY;
			super.visitTableSwitchInsn(min, max, dflt, labels);
		}

		@Override
		public void visitTryCatchBlock(final Label start, final Label end,
				final Label handler, final String type) {
			methodState = State.NOT_EMPTY;
			super.visitTryCatchBlock(start, end, handler, type);
		}

		@Override
		public void visitTypeInsn(final int opcode, final String type) {
			methodState = State.NOT_EMPTY;
			super.visitTypeInsn(opcode, type);
		}
	}

	public MethodProbesVisitor visitMethod(final String name,
			final String signature, final MethodProbesVisitor delegate) {
		if (methodState == State.EMPTY) {
			enabled = false;
		} else {
			enabled = true;
		}
		return delegate;
	}
}
