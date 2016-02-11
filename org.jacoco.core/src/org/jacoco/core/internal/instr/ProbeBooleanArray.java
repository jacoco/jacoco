/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Arrays;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.data.CompactDataInput;
import org.jacoco.core.internal.data.CompactDataOutput;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A container object for all probes. This is object is both lock-free and
 * thread-safe.
 * 
 * @author Omer Azmon
 */
public final class ProbeBooleanArray implements IProbeArray<boolean[]> {

	private static final String DATAFIELD_CLASS = "[Z";
	private static final String DATAFIELD_DESC = "[Z";
	private static final String INITMETHOD_DESC = "()" + DATAFIELD_DESC;

	public char getFormatVersion() {
		return ExecutionDataWriter.FORMAT_VERSION;
	}

	public ProbeMode getProbeMode() {
		return ProbeMode.exists;
	}

	public String getDatafieldClass() {
		return DATAFIELD_CLASS;
	}

	public String getDatafieldDesc() {
		return DATAFIELD_DESC;
	}

	public String getInitMethodDesc() {
		return INITMETHOD_DESC;
	}

	public ProbeBooleanArray read(final CompactDataInput input)
			throws IOException {
		return new ProbeBooleanArray(input.readBooleanArray());
	}

	private static final Object[] FRAME_STACK_ARRZ = new Object[] { DATAFIELD_CLASS };
	private static final Object[] FRAME_LOCALS_EMPTY = new Object[0];

	public void createInitMethod(final ClassVisitor cv, final long classId,
			final String className, final boolean withFrames,
			final IExecutionDataAccessorGenerator accessorGenerator,
			final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, INITMETHOD_DESC, null, null);
		mv.visitCode();

		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				InstrSupport.DATAFIELD_NAME, DATAFIELD_DESC);
		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Z
		// Stack[0]: [Z

		// Skip initialization when we already have a data array:
		final Label alreadyInitialized = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);

		// Stack[0]: [Z

		mv.visitInsn(Opcodes.POP);
		final int size = genInitializeDataField(mv, classId, className,
				accessorGenerator, probeCount);

		// Stack[0]: [Z

		// Return the class' probe array:
		if (withFrames) {
			mv.visitFrame(Opcodes.F_NEW, 0, FRAME_LOCALS_EMPTY, 1,
					FRAME_STACK_ARRZ);
		}
		mv.visitLabel(alreadyInitialized);
		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(Math.max(size, 2), 0); // Maximum local stack size is 2
		mv.visitEnd();
	}

	private int genInitializeDataField(final MethodVisitor mv,
			final long classId, final String className,
			final IExecutionDataAccessorGenerator accessorGenerator,
			final int probeCount) {
		final int size = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);

		// Stack[0]: [Z

		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Z
		// Stack[0]: [Z

		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				InstrSupport.DATAFIELD_NAME, DATAFIELD_DESC);

		// Stack[0]: [Z

		return Math.max(size, 2); // Maximum local stack size is 2
	}

	public int incrementProbeStackSize() {
		return 3;
	}

	public void insertProbe(final MethodVisitor mv, final int variable,
			final int id) {

		// If not already on stack, ...
		if (variable >= 0) {
			mv.visitVarInsn(Opcodes.ALOAD, variable);
		}

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

	public MethodVisitor addProbeAdvisor(final MethodVisitor mv,
			final int access, final String name, final String desc) {
		return mv;
	}

	/* ***************************************************************** */

	private final boolean[] probes;

	/**
	 * Constructor
	 * 
	 * @param size
	 *            the number of probes
	 */
	public ProbeBooleanArray(final int size) {
		this(new boolean[size]);
	}

	/**
	 * Constructor
	 * 
	 * @param size
	 *            the number of probes
	 */
	private ProbeBooleanArray(final boolean[] probes) {
		this.probes = probes;
	}

	public ProbeBooleanArray newProbeArray(final int size) {
		return new ProbeBooleanArray(size);
	}

	public ProbeBooleanArray newProbeArray(final Object object) {
		if (object == null || !(object instanceof boolean[])) {
			throw new IllegalArgumentException();
		}
		return new ProbeBooleanArray((boolean[]) object);
	}

	public ProbeBooleanArray copy() {
		return new ProbeBooleanArray(probes.clone());
	}

	public int length() {
		return probes.length;
	}

	public final void increment(final int probeId) {
		probes[probeId] = true;
	}

	public final void reset() {
		Arrays.fill(probes, false);
	}

	public final boolean[] getProbesObject() {
		return probes;
	}

	public boolean isProbeCovered(final int index) {
		return probes[index];
	}

	public int getExecutionProbe(final int index) {
		return 0;
	}

	public int getParallelExecutionProbe(final int index) {
		return 0;
	}

	public void merge(final IProbeArray<?> otherObject, final boolean flag) {
		assertCompatibility(otherObject);
		final ProbeBooleanArray other = (ProbeBooleanArray) otherObject;
		for (int i = 0; i < this.probes.length; i++) {
			if (other.probes[i]) {
				this.probes[i] = flag;
			}
		}
	}

	public void assertCompatibility(final IProbeArray<?> otherObject) {
		if (this.getClass() != otherObject.getClass()) {
			throw new IllegalArgumentException(format(
					"Unable to merge probe data in %s with data in %s.", this
							.getClass().getName(), otherObject.getClass()
							.getName()));
		}
		final ProbeBooleanArray other = (ProbeBooleanArray) otherObject;

		if (this.probes.length != other.probes.length) {
			throw new IllegalStateException(
					"Unable to merge probe data probes with different counts.");
		}
	}

	public void write(final char formatVersion, final CompactDataOutput output)
			throws IOException {
		if (formatVersion != ExecutionDataWriter.FORMAT_VERSION) {
			throw new IOException("Unable to write boolean probes in format 0x"
					+ Integer.toHexString(formatVersion));
		}
		output.writeBooleanArray(probes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(probes);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ProbeBooleanArray other = (ProbeBooleanArray) obj;
		return Arrays.equals(probes, other.probes);
	}

	@Override
	public String toString() {
		return "ProbeBooleanArray" + Arrays.toString(probes);
	}

}