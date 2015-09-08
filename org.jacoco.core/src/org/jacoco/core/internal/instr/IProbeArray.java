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

import java.io.IOException;

import org.jacoco.core.data.IProbes;
import org.jacoco.core.internal.data.CompactDataInput;
import org.jacoco.core.internal.data.CompactDataOutput;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A container object for all probes. Implementation MUST be both lock-free and
 * thread-safe.
 * 
 * @param <T>
 *            the type of the probe array, such as, boolean[], int[], or int[][]
 * 
 * @author Omer Azmon
 */
public interface IProbeArray<T> extends IProbes {

	/**
	 * Returns the unique id name that identifies this array type implementation
	 * both in the ExecutionData files.
	 * 
	 * @return the unique type name that identifies this array type
	 */
	public byte getTypeId();

	// InstrSupport
	/**
	 * Return the class name in 'slash' format that stores coverage information
	 * for the of probe array. This is the same class as identified by the
	 * {@code<T>} parameter.
	 * 
	 * @return the class name in 'slash' format.
	 */
	public String getDatafieldClass();

	/**
	 * Return the data type of the field that stores coverage information for a
	 * class. For primitive types and their arrays, it is the same as the data
	 * field class.
	 * 
	 * @return the data type of the field that stores coverage information
	 */
	public String getDatafieldDesc();

	// public static final String INITMETHOD_DESC = "()" + DATAFIELD_DESC;

	/**
	 * Returns descriptor of the initialization method.
	 * 
	 * @return descriptor of the initialization method.
	 */
	public String getInitMethodDesc();

	// FieldProbeArrayStrategy
	/**
	 * Generates the byte code to initialize the static coverage data field
	 * within this class.
	 * 
	 * The code will push the probe data array on the operand stack.
	 * 
	 * @param cv
	 *            the class visitor of the class "poked"
	 * @param classId
	 *            the CRC of the class bytes
	 * @param className
	 *            the class name in the class loader
	 * @param withFrames
	 *            <code>true</code> if
	 *            <code>{@literal version >= Opcodes.V1_6}</code>
	 * @param accessorGenerator
	 *            accessor to the coverage runtime
	 * @param probeCount
	 *            the number of probes in this class
	 */
	public void createInitMethod(final ClassVisitor cv, final long classId,
			final String className, final boolean withFrames,
			final IExecutionDataAccessorGenerator accessorGenerator,
			final int probeCount);

	// ProbeInserter
	/**
	 * Generates the byte code to perform an increment on a probe. This
	 * implement by having the byte code invoke the {@code increment} method of
	 * the object.
	 * 
	 * @param mv
	 *            the method visitor of the method "poked"
	 * @param variable
	 *            position of inserted variable
	 * @param id
	 *            the id of the probe to update
	 */
	public void insertProbe(final MethodVisitor mv, final int variable,
			final int id);

	/**
	 * Returns the stack size required by an increment probe operation of this
	 * type.
	 * 
	 * @return the stack size required by an increment probe operation
	 */
	public int incrementProbeStackSize();

	/**
	 * Create a probe array object of this type.
	 * 
	 * @param size
	 *            the number of elements in the array
	 * @return the probe array object
	 */
	public IProbeArray<T> newProbeArray(int size);

	/**
	 * Create a probe array object of this type.
	 * 
	 * @param dataObject
	 *            the internal probe data object
	 * 
	 * @return the probe array object
	 * @throws IllegalArgumentException
	 *             if {@code dataObject} is does not match the type accepted by
	 *             this implementation.
	 */
	public IProbeArray<T> newProbeArray(Object dataObject);

	// ProbeArray
	/**
	 * Increment a probe. The method is used by the byte code to increment a
	 * probe.
	 * 
	 * @param probeId
	 *            the probe id must be between zero and the number of probes
	 *            given at construction.
	 */
	public void increment(final int probeId);

	/**
	 * Set all probes to zero.
	 */
	public void reset();

	/**
	 * Get the object injected into class initializer
	 * 
	 * @return the object injected into class initializer
	 */
	public T getProbesObject();

	// ExecutionData
	/**
	 * Merges the given execution data into the probe data of this object. The
	 * probes in this object is set sum/subtraction of the corresponding probes
	 * in the other object, depending on the value of <code>flag</code>. For
	 * <code>{@literal flag==true}</code> this is an addition and for
	 * <code>boolean</code> corresponds to
	 * 
	 * <pre>
	 * A + B
	 * </pre>
	 * 
	 * For <code>{@literal flag==true}</code> this can be considered as a
	 * subtraction and for <code>boolean</code> corresponds to
	 * 
	 * <pre>
	 * A and not B
	 * </pre>
	 * 
	 * The probe array of the other object is not modified.
	 * 
	 * @param other
	 *            probe data to merge
	 * @param flag
	 *            merge mode. <code>true</code> for add; <code>false</code> for
	 *            subtract.
	 */
	public void merge(final IProbeArray<?> other, final boolean flag);

	// ExecutionData
	/**
	 * Asserts that a probe object is compatible (can be merged) with this
	 * object.
	 * 
	 * @param other
	 *            probe data length, must be the same as for this data
	 * @throws IllegalStateException
	 *             if the given parameters do not match this instance
	 */
	public void assertCompatibility(final IProbeArray<?> other);

	/**
	 * Read a {@code IProbeArray} from an input stream. The input stream must be
	 * positioned at the first byte of data.
	 * 
	 * @param inputStream
	 *            the compact data input stream to read
	 * @return a Probe array object constructed from the data read.
	 * @throws IOException
	 *             on any failure to read.
	 */
	// ExecutionDataReader
	public IProbeArray<T> read(final CompactDataInput inputStream)
			throws IOException;

	// ExecutionDataWriter
	/**
	 * Write a probe array. The array written can be read with the read method
	 * above. No other guarantees exist.
	 * 
	 * @param output
	 *            the output stream.
	 * @throws IOException
	 *             on any failure to write.
	 */
	public void write(final CompactDataOutput output) throws IOException;

	// ClassInstrumenter
	/**
	 * Wrap the method visitor with a probe style specific advisor, as needed.
	 * 
	 * @param mv
	 *            the method visitor to wrap
	 * @param access
	 *            the method's access flags (see {@link Opcodes}). This
	 *            parameter also indicates if the method is synthetic and/or
	 *            deprecated.
	 * @param name
	 *            the method's name.
	 * @param desc
	 *            the method's descriptor (see {@link Type Type}).
	 * @return the method visitor optionally wrapped with an advisor
	 */
	public MethodVisitor addProbeAdvisor(final MethodVisitor mv,
			final int access, final String name, final String desc);

}