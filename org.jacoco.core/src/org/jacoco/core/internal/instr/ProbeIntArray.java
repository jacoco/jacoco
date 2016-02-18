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
import java.util.concurrent.atomic.AtomicIntegerArray;

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
public final class ProbeIntArray implements IProbeArray<AtomicIntegerArray> {
	private static final String DATAFIELD_CLASS = "java/util/concurrent/atomic/AtomicIntegerArray";
	private static final String DATAFIELD_DESC = "Ljava/util/concurrent/atomic/AtomicIntegerArray;";
	private static final String INITMETHOD_DESC = "()" + DATAFIELD_DESC;

	public char getFormatVersion() {
		return ExecutionDataWriter.FORMAT_INT_VERSION;
	}

	public ProbeMode getProbeMode() {
		return ProbeMode.count;
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

	public ProbeIntArray read(final CompactDataInput input) throws IOException {
		return new ProbeIntArray(new AtomicIntegerArray(input.readIntArray()));
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
		// Stack[0]: Ljava/util/concurrent/atomic/AtomicIntegerArray;

		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: Ljava/util/concurrent/atomic/AtomicIntegerArray;
		// Stack[0]: Ljava/util/concurrent/atomic/AtomicIntegerArray;

		// Skip initialization when we already have a data array:
		final Label alreadyInitialized = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);

		// Stack[0]: Ljava/util/concurrent/atomic/AtomicIntegerArray;

		mv.visitInsn(Opcodes.POP);
		final int size = genInitializeDataField(mv, classId, className,
				accessorGenerator, probeCount);

		// Stack[0]: Ljava/util/concurrent/atomic/AtomicIntegerArray;

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

		// Stack[0]: Ljava/util/concurrent/atomic/AtomicIntegerArray;

		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: Ljava/util/concurrent/atomic/AtomicIntegerArray;
		// Stack[0]: Ljava/util/concurrent/atomic/AtomicIntegerArray;

		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				InstrSupport.DATAFIELD_NAME, DATAFIELD_DESC);

		// Stack[0]: Ljava/util/concurrent/atomic/AtomicIntegerArray;

		return Math.max(size, 2); // Maximum local stack size is 2
	}

	public void insertProbe(final MethodVisitor mv, final int variable,
			final int id) {
		// For a probe we increment the corresponding position in the
		// AtomicIntegerArray.

		// If not already on stack, ...
		if (variable >= 0) {
			mv.visitVarInsn(Opcodes.ALOAD, variable);
		}

		// Stack[0]: Ljava/util/concurrent/atomic/AtomicIntegerArray;

		InstrSupport.push(mv, id);

		// Stack[1]: I
		// Stack[0]: Ljava/util/concurrent/atomic/AtomicIntegerArray;

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"java/util/concurrent/atomic/AtomicIntegerArray",
				"incrementAndGet", "(I)I", false);

		// Stack[0]: I
		mv.visitInsn(Opcodes.POP);
	}

	public int incrementProbeStackSize() {
		return 2;
	}

	public MethodVisitor addProbeAdvisor(final MethodVisitor mv,
			final int access, final String name, final String desc) {
		return mv;
	}

	/* ***************************************************************** */

	private final AtomicIntegerArray probes;

	/**
	 * Constructor
	 * 
	 * @param size
	 *            the number of probes
	 */
	public ProbeIntArray(final int size) {
		this(new AtomicIntegerArray(size));
	}

	/**
	 * Constructor
	 * 
	 * @param size
	 *            the number of probes
	 */
	private ProbeIntArray(final AtomicIntegerArray probes) {
		this.probes = probes;
	}

	public ProbeIntArray newProbeArray(final int size) {
		return new ProbeIntArray(size);
	}

	public ProbeIntArray newProbeArray(final Object object) {
		if (object == null || !(object instanceof AtomicIntegerArray)) {
			throw new IllegalArgumentException();
		}
		return new ProbeIntArray((AtomicIntegerArray) object);
	}

	public ProbeIntArray copy() {
		final AtomicIntegerArray newProbes = new AtomicIntegerArray(
				probes.length());
		for (int ix = 0; ix < probes.length(); ix++) {
			newProbes.set(ix, probes.get(ix));
		}
		return new ProbeIntArray(newProbes);
	}

	public int length() {
		return probes.length();
	}

	public final void increment(final int probeId) {
		probes.incrementAndGet(probeId);
	}

	public final void reset() {
		for (int ix = 0; ix < probes.length(); ix++) {
			probes.set(ix, 0);
		}
	}

	public final AtomicIntegerArray getProbesObject() {
		return probes;
	}

	public boolean isProbeCovered(final int index) {
		return probes.get(index) > 0;
	}

	public int getExecutionProbe(final int index) {
		return probes.get(index);
	}

	public int getParallelExecutionProbe(final int index) {
		return 0;
	}

	public void merge(final IProbeArray<?> otherObject, final boolean flag) {
		assertCompatibility(otherObject);
		final ProbeIntArray other = (ProbeIntArray) otherObject;
		for (int i = 0; i < this.probes.length(); i++) {
			final int otherValue = other.probes.get(i);
			if (otherValue > 0) {
				final int thisValue = this.probes.get(i);
				final int newValue = flag ? thisValue + otherValue //
				: thisValue - otherValue;
				this.probes.set(i, newValue > 0 ? newValue : 0);
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
		final ProbeIntArray other = (ProbeIntArray) otherObject;

		if (this.probes.length() != other.probes.length()) {
			throw new IllegalStateException(
					"Unable to merge probe data probes with different counts.");
		}
	}

	public void write(final char formatVersion, final CompactDataOutput output)
			throws IOException {
		if (formatVersion != ExecutionDataWriter.FORMAT_INT_VERSION) {
			throw new IOException("Unable to write int probes in format 0x"
					+ Integer.toHexString(formatVersion));
		}
		final int[] probes = new int[this.probes.length()];
		for (int ix = 0; ix < this.probes.length(); ix++) {
			probes[ix] = this.probes.get(ix);
		}
		output.writeIntArray(probes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result;
		for (int ix = 0; ix < length(); ix++) {
			final int element = getExecutionProbe(ix);
			final int elementHash = element ^ (element >>> 32);
			result = prime * result + elementHash;
		}
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
		final ProbeIntArray that = (ProbeIntArray) obj;
		for (int ix = 0; ix < length(); ix++) {
			final int thisElement = this.getExecutionProbe(ix);
			final int thatElement = that.getExecutionProbe(ix);
			if (thisElement != thatElement) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		final int[] probes = new int[length()];
		for (int ix = 0; ix < length(); ix++) {
			probes[ix] = this.getExecutionProbe(ix);
		}
		return "ProbeIntArray" + Arrays.toString(probes);
	}
}