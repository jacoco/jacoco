/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

/**
 * Internal utility to add probes into the control flow of a method. The code
 * for a probe simply sets a certain slot of a boolean array to true. In
 * addition the probe array has to be retrieved at the beginning of the method
 * and stored in a local variable.
 */
class ProbeInserter extends MethodVisitor implements IProbeInserter {

	private final IProbeArrayStrategy arrayStrategy;

	/**
	 * <code>true</code> if method is a class or interface initialization
	 * method.
	 */
	private final boolean clinit;

	/** Position of the inserted variable. */
	private final int variable;

	/** Label for the new beginning of the method */
	private final Label beginLabel;

	/** Maximum stack usage of the code to access the probe array. */
	private int accessorStackSize;

	/**
	 * Creates a new {@link ProbeInserter}.
	 *
	 * @param access
	 *            access flags of the adapted method
	 * @param name
	 *            the method's name
	 * @param desc
	 *            the method's descriptor
	 * @param mv
	 *            the method visitor to which this adapter delegates calls
	 * @param arrayStrategy
	 *            callback to create the code that retrieves the reference to
	 *            the probe array
	 */
	ProbeInserter(final int access, final String name, final String desc,
			final MethodVisitor mv, final IProbeArrayStrategy arrayStrategy) {
		super(InstrSupport.ASM_API_VERSION, mv);
		this.clinit = InstrSupport.CLINIT_NAME.equals(name);
		this.arrayStrategy = arrayStrategy;
		int pos = (Opcodes.ACC_STATIC & access) == 0 ? 1 : 0;
		for (final Type t : Type.getArgumentTypes(desc)) {
			pos += t.getSize();
		}
		variable = pos;
		beginLabel = new Label();
	}

	public void insertProbe(final int id) {

		// For a probe we set the corresponding position in the boolean[] array
		// to true.

		mv.visitVarInsn(Opcodes.ALOAD, variable);

		// Stack[0]: [Z

		InstrSupport.push(mv, id);

		// Stack[1]: I
		// Stack[0]: [Z

		mv.visitInsn(Opcodes.ICONST_1);

		// Stack[2]: I
		// Stack[1]: I
		// Stack[0]: [Z

		mv.visitInsn(Opcodes.BASTORE);
	}

	@Override
	public void visitCode() {
		mv.visitLabel(beginLabel);
		accessorStackSize = arrayStrategy.storeInstance(mv, clinit, variable);
		mv.visitCode();
	}

	@Override
	public final void visitVarInsn(final int opcode, final int var) {
		mv.visitVarInsn(opcode, map(var));
	}

	@Override
	public final void visitIincInsn(final int var, final int increment) {
		mv.visitIincInsn(map(var), increment);
	}

	@Override
	public final void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		if (index < variable) {
			// Method parameters are still valid from the very beginning
			mv.visitLocalVariable(name, desc, signature, beginLabel, end,
					index);
		} else {
			mv.visitLocalVariable(name, desc, signature, start, end,
					map(index));
		}
	}

	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef,
			final TypePath typePath, final Label[] start, final Label[] end,
			final int[] index, final String descriptor, final boolean visible) {
		final int[] newIndex = new int[index.length];
		for (int i = 0; i < newIndex.length; i++) {
			newIndex[i] = map(index[i]);
		}
		return mv.visitLocalVariableAnnotation(typeRef, typePath, start, end,
				newIndex, descriptor, visible);
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		// Max stack size of the probe code is 3 which can add to the
		// original stack size depending on the probe locations. The accessor
		// stack size is an absolute maximum, as the accessor code is inserted
		// at the very beginning of each method when the stack size is empty.
		final int increasedStack = Math.max(maxStack + 3, accessorStackSize);
		mv.visitMaxs(increasedStack, maxLocals + 1);
	}

	private int map(final int var) {
		if (var < variable) {
			return var;
		} else {
			return var + 1;
		}
	}

	@Override
	public final void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {

		if (type != Opcodes.F_NEW) { // uncompressed frame
			throw new IllegalArgumentException(
					"ClassReader.accept() should be called with EXPAND_FRAMES flag");
		}

		final Object[] newLocal = new Object[Math.max(nLocal, variable) + 1];
		int idx = 0; // Arrays index for existing locals
		int newIdx = 0; // Array index for new locals
		int pos = 0; // Current variable position
		while (idx < nLocal || pos <= variable) {
			if (pos == variable) {
				newLocal[newIdx++] = InstrSupport.DATAFIELD_DESC;
				pos++;
			} else {
				if (idx < nLocal) {
					final Object t = local[idx++];
					newLocal[newIdx++] = t;
					pos++;
					if (t == Opcodes.LONG || t == Opcodes.DOUBLE) {
						pos++;
					}
				} else {
					// Fill unused slots with TOP
					newLocal[newIdx++] = Opcodes.TOP;
					pos++;
				}
			}
		}
		mv.visitFrame(type, newIdx, newLocal, nStack, stack);
	}

}
