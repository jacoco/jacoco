/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.JaCoCo;
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
public final class ProbeDoubleIntArray implements
		IProbeArray<ProbeDoubleIntArray> {
	/** The Unique ID of this Probe mode in an execution data file */
	public static final byte PROBE_TYPE_ID = 3;

	// NOTE!!! THIS MUST BE DONE THIS WAY AS THE AGENT DOES A REWRITE OF ITS
	// OBJECTS INTO A SAFE SPACE
	private static final String DATAFIELD_CLASS = JaCoCo.RUNTIMEPACKAGE
			.replace('.', '/') + "/core/internal/instr/ProbeDoubleIntArray";
	private static final String DATAFIELD_DESC = "L" + DATAFIELD_CLASS + ";";
	private static final String INITMETHOD_DESC = "()" + DATAFIELD_DESC;

	public byte getTypeId() {
		return PROBE_TYPE_ID;
	}

	public ProbeMode getProbeMode() {
		return ProbeMode.parallelcount;
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

	public ProbeDoubleIntArray read(final CompactDataInput input)
			throws IOException {
		return new ProbeDoubleIntArray(new AtomicIntegerArray(
				input.readIntArray()), new AtomicIntegerArray(
				input.readIntArray()));
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
		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;

		mv.visitInsn(Opcodes.DUP);
		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;
		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;

		// Skip initialization when we already have a data array:
		final Label alreadyInitialized = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);
		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;

		mv.visitInsn(Opcodes.POP);

		final int size = genInitializeDataField(mv, classId, className,
				accessorGenerator, probeCount);

		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;

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

		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;

		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;
		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;

		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				InstrSupport.DATAFIELD_NAME, DATAFIELD_DESC);

		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;

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

		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;

		InstrSupport.push(mv, id);

		// Stack[1]: I
		// Stack[0]: Lorg/jacoco/core/internal/probe/ProbeDoubleIntArray;

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DATAFIELD_CLASS, "increment",
				"(I)V", false);
	}

	public int incrementProbeStackSize() {
		return 2;
	}

	public MethodVisitor addProbeAdvisor(final MethodVisitor mv,
			final int access, final String name, final String desc) {
		return new MonitorCounterAdvisor(JaCoCo.ASM_API_VERSION, mv, access,
				name, desc);
	}

	/* ***************************************************************** */

	private final AtomicIntegerArray probes;
	private final AtomicIntegerArray parallelProbes;

	/**
	 * Constructor
	 * 
	 * @param size
	 *            the number of probes
	 */
	public ProbeDoubleIntArray(final int size) {
		this(new AtomicIntegerArray(size), new AtomicIntegerArray(size));
	}

	/**
	 * Constructor
	 * 
	 * @param size
	 *            the number of probes
	 */
	private ProbeDoubleIntArray(final AtomicIntegerArray probes,
			final AtomicIntegerArray parallelProbes) {
		this.probes = probes;
		this.parallelProbes = parallelProbes;
	}

	public ProbeDoubleIntArray newProbeArray(final Object object) {
		if (object == null || !(object instanceof ProbeDoubleIntArray)) {
			throw new IllegalArgumentException();
		}
		return (ProbeDoubleIntArray) object;
	}

	public ProbeDoubleIntArray newProbeArray(final int size) {
		return new ProbeDoubleIntArray(size);
	}

	public ProbeDoubleIntArray copy() {
		final AtomicIntegerArray newProbes = new AtomicIntegerArray(
				probes.length());
		for (int ix = 0; ix < probes.length(); ix++) {
			newProbes.set(ix, probes.get(ix));
		}
		final AtomicIntegerArray newParallelProbes = new AtomicIntegerArray(
				parallelProbes.length());
		for (int ix = 0; ix < parallelProbes.length(); ix++) {
			newParallelProbes.set(ix, parallelProbes.get(ix));
		}
		return new ProbeDoubleIntArray(newProbes, newParallelProbes);
	}

	public int length() {
		return probes.length();
	}

	public final void increment(final int probeId) {
		probes.incrementAndGet(probeId);
		if (ThreadLocalMonitorCounter.isNoLock()) {
			parallelProbes.incrementAndGet(probeId);
		}
	}

	public final void reset() {
		for (int ix = 0; ix < probes.length(); ix++) {
			probes.set(ix, 0);
		}
		for (int ix = 0; ix < parallelProbes.length(); ix++) {
			parallelProbes.set(ix, 0);
		}
	}

	public final ProbeDoubleIntArray getProbesObject() {
		return this;
	}

	public boolean isProbeCovered(final int index) {
		return probes.get(index) > 0;
	}

	public int getExecutionProbe(final int index) {
		return probes.get(index);
	}

	public int getParallelExecutionProbe(final int index) {
		return parallelProbes.get(index);
	}

	public void merge(final IProbeArray<?> otherObject, final boolean flag) {
		assertCompatibility(otherObject);
		final ProbeDoubleIntArray other = (ProbeDoubleIntArray) otherObject;
		for (int i = 0; i < this.probes.length(); i++) {
			{
				final int otherValue = other.probes.get(i);
				if (otherValue > 0) {
					final int thisValue = this.probes.get(i);
					final int newValue = flag ? thisValue + otherValue //
					: thisValue - otherValue;
					this.probes.set(i, newValue > 0 ? newValue : 0);
				}
			}
			{
				final int otherParallelValue = other.parallelProbes.get(i);
				if (otherParallelValue > 0) {
					final int thisParalleValue = this.parallelProbes.get(i);
					final int newParallelValue = flag ? thisParalleValue
							+ otherParallelValue //
					: thisParalleValue - otherParallelValue;
					this.parallelProbes.set(i,
							newParallelValue > 0 ? newParallelValue : 0);
				}
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
		final ProbeDoubleIntArray other = (ProbeDoubleIntArray) otherObject;

		if (this.probes.length() != other.probes.length()) {
			throw new IllegalStateException(
					"Unable to merge probe data probes with different counts.");
		}
	}

	public void write(final CompactDataOutput output) throws IOException {
		output.writeByte(PROBE_TYPE_ID);
		final int[] probes = new int[this.probes.length()];
		for (int ix = 0; ix < this.probes.length(); ix++) {
			probes[ix] = this.probes.get(ix);
		}
		output.writeIntArray(probes);
		final int[] parallelProbes = new int[this.parallelProbes.length()];
		for (int ix = 0; ix < this.parallelProbes.length(); ix++) {
			parallelProbes[ix] = this.parallelProbes.get(ix);
		}
		output.writeIntArray(parallelProbes);
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
		final ProbeDoubleIntArray that = (ProbeDoubleIntArray) obj;
		for (int ix = 0; ix < length(); ix++) {
			{
				final int thisElement = this.getExecutionProbe(ix);
				final int thatElement = that.getExecutionProbe(ix);
				if (thisElement != thatElement) {
					return false;
				}
			}
			{
				final int thisElement = this.getParallelExecutionProbe(ix);
				final int thatElement = that.getParallelExecutionProbe(ix);
				if (thisElement != thatElement) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public String toString() {
		final int[] probes = new int[length()];
		final int[] parallelProbes = new int[length()];
		for (int ix = 0; ix < length(); ix++) {
			probes[ix] = this.getExecutionProbe(ix);
			parallelProbes[ix] = this.getParallelExecutionProbe(ix);
		}
		return "ProbeDoubleIntArray" + Arrays.toString(probes) + ", "
				+ Arrays.toString(parallelProbes);
	}
}