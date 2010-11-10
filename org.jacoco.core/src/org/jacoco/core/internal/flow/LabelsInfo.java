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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Utility class to collect flow related information about the {@link Label}s
 * within a class. After initialization it provides the following information
 * about labels:
 * 
 * <ul>
 * <li>Multi Target: Is a given label the target of multiple control flow paths?
 * Control flow path to a certain label are: jump targets, exception handlers
 * and normal control flow from its predecessor instruction (unless this a
 * unconditional jump or method exit).</li>
 * <li>Successor: Can a given label be a instruction successor in the the normal
 * control flow of a method? This is the case if the predecessor isn't a
 * unconditional jump or method exit.</li>
 * </ul>
 * 
 * Before the query method in this class can be used it has first to be
 * populated with the method data through its {@link MethodVisitor} interface.
 * Note that this class stores information in the {@link Label#info}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
final class LabelsInfo implements MethodVisitor {

	private static enum Status {
		NONE() {
			@Override
			Status target() {
				return TARGET;
			}

			@Override
			Status successor() {
				return SUCCESSOR;
			}

			@Override
			boolean isMultiTarget() {
				return false;
			}

			@Override
			boolean isSuccessor() {
				return false;
			}
		},
		TARGET() {
			@Override
			Status target() {
				return MULTITARGET;
			}

			@Override
			Status successor() {
				return Status.MULTITARGETSUCCESSOR;
			}

			@Override
			boolean isMultiTarget() {
				return false;
			}

			@Override
			boolean isSuccessor() {
				return false;
			}
		},
		SUCCESSOR() {
			@Override
			Status target() {
				return MULTITARGETSUCCESSOR;
			}

			@Override
			boolean isMultiTarget() {
				return false;
			}

			@Override
			boolean isSuccessor() {
				return true;
			}
		},
		MULTITARGET() {
			@Override
			Status successor() {
				return MULTITARGETSUCCESSOR;
			}

			@Override
			boolean isMultiTarget() {
				return true;
			}

			@Override
			boolean isSuccessor() {
				return false;
			}
		},
		MULTITARGETSUCCESSOR() {
			@Override
			boolean isMultiTarget() {
				return true;
			}

			@Override
			boolean isSuccessor() {
				return true;
			}
		};

		Status target() {
			return this;
		}

		Status successor() {
			return this;
		}

		abstract boolean isMultiTarget();

		abstract boolean isSuccessor();

	}

	private static Status getStatus(final Label l) {
		final Object info = l.info;
		return info == null ? Status.NONE : (Status) info;
	}

	private static void target(final Label l) {
		l.info = getStatus(l).target();
	}

	private static void target(final Label[] labels) {
		for (final Label l : labels) {
			target(l);
		}
	}

	private static void successor(final Label l) {
		l.info = getStatus(l).successor();
	}

	// visible for testing
	/* package */boolean successor = true;

	/**
	 * Checks whether multiple control paths lead to a label
	 * 
	 * @param label
	 *            label to check
	 * @return <code>true</code> if the given multiple control paths lead to the
	 *         given label
	 */
	public static boolean isMultiTarget(final Label label) {
		return getStatus(label).isMultiTarget();
	}

	/**
	 * Checks whether this label is the possible successor of the previous
	 * instruction in the method.
	 * 
	 * @param label
	 *            label to check
	 * @return <code>true</code> if the label is a possible instruction
	 *         successor
	 */
	public static boolean isSuccessor(final Label label) {
		return getStatus(label).isSuccessor();
	}

	// === MethodVisitor ===

	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		target(handler);
	}

	public void visitInsn(final int opcode) {
		switch (opcode) {
		case Opcodes.IRETURN:
		case Opcodes.LRETURN:
		case Opcodes.FRETURN:
		case Opcodes.DRETURN:
		case Opcodes.ARETURN:
		case Opcodes.RETURN:
		case Opcodes.ATHROW:
			successor = false;
			break;
		default:
			successor = true;
			break;
		}
	}

	public void visitIntInsn(final int opcode, final int operand) {
		successor = true;
	}

	public void visitVarInsn(final int opcode, final int var) {
		successor = true;
	}

	public void visitTypeInsn(final int opcode, final String type) {
		successor = true;
	}

	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		successor = true;
	}

	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		successor = true;
	}

	public void visitJumpInsn(final int opcode, final Label label) {
		target(label);
		successor = opcode != Opcodes.GOTO;
	}

	public void visitLabel(final Label label) {
		if (successor) {
			successor(label);
		}
	}

	public void visitLdcInsn(final Object cst) {
		successor = true;
	}

	public void visitIincInsn(final int var, final int increment) {
		successor = true;
	}

	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
		target(dflt);
		target(labels);
		successor = false;
	}

	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		target(dflt);
		target(labels);
		successor = false;
	}

	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		successor = true;
	}

	// Not relevant:

	public void visitAttribute(final Attribute attr) {
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		return null;
	}

	public AnnotationVisitor visitParameterAnnotation(final int parameter,
			final String desc, final boolean visible) {
		return null;
	}

	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
	}

	public void visitCode() {
	}

	public void visitLineNumber(final int line, final Label start) {
	}

	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {
	}

	public void visitMaxs(final int maxStack, final int maxLocals) {
	}

	public void visitEnd() {
	}

}
