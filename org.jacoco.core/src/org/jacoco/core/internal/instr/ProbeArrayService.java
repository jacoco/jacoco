/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.data.CompactDataInput;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * An Java service for providers that create different types of data collection
 * probes. By using this abstraction we can continue to use by default
 * boolean[], but provide more advanced features to those that choose so.
 * <p>
 * Implementations of the service must {@code implement IProbeArray} interface,
 * and be published in @{code
 * META-INF/services/org.jacoco.core.internal.probe.spi.IProbeArray} file. If,
 * for example, @{code com.example.MyProbeArray} implements a new type of probe
 * array, then the jar containing it must also contain a file name @{code
 * META-INF/services/org.jacoco.core.internal.probe.spi.IProbeArray}, and this
 * file must contain a line @{code com.example.MyProbeArray}. The file may
 * contain more than one entry and have in-line and line comments preceeded with
 * '#'. For more information see {@link java.util.ServiceLoader}.
 * 
 * @author Omer Azmon
 */
public final class ProbeArrayService {

	private static ProbeArrayService service;

	/**
	 * Configure this service as to the general type of array to provide the
	 * core system for embedding in the byte code.
	 * 
	 * @param requestedProbeMode
	 *            the probe mode desired.
	 */
	public static void configure(final ProbeMode requestedProbeMode) {
		configureInternal(requestedProbeMode);
		if (requestedProbeMode != null) {
			if (requestedProbeMode != service.probeMode) {
				throw new IllegalStateException(
						"Unable to configure service default as "
								+ requestedProbeMode.name()
								+ " as service is already configured as "
								+ service.probeMode.name());
			}
		}

	}

	private static void configureInternal(final ProbeMode requestedProbeMode) {
		if (service == null) {
			synchronized (ProbeArrayService.class) {
				if (service == null) {
					service = new ProbeArrayService(requestedProbeMode);
				}
			}
		}
	}

	private static ProbeArrayService instance() {
		configureInternal(null);
		return service;
	}

	/**
	 * Reset to un-configured state
	 */
	public synchronized static void reset() {
		service = null;
	}

	private final ProbeMode probeMode;

	private ProbeArrayService(final ProbeMode probeMode) {
		this.probeMode = probeMode == null ? ProbeMode.exists : probeMode;
	}

	private static IProbeArray<?> getProbeZeroInstance() {
		return instance().probeMode.getProbeZeroInstance();
	}

	/**
	 * Returns the Probe Array type that has been configured using the
	 * {@code configure} static method, or by default.
	 * 
	 * @return the Probe Array type that has been configured
	 */
	public static ProbeMode getProbeMode() {
		return instance().probeMode;
	}

	/**
	 * Create a probe array object of the configured type.
	 * 
	 * @param size
	 *            the number of elements in the array
	 * @return the probe array object
	 */
	public static IProbeArray<?> newProbeArray(final int size) {
		return getProbeZeroInstance().newProbeArray(size);

	}

	/**
	 * Create a probe array object type based on a data object
	 * 
	 * @param dataObject
	 *            the internal probe data object that determines the type of
	 *            probe array object created.
	 * @return the probe array object
	 * @throws IllegalArgumentException
	 *             if can't find any match for the dataObject
	 */
	public static IProbeArray<?> newProbeArray(final Object dataObject) {
		if (dataObject instanceof IProbeArray) {
			return (IProbeArray<?>) dataObject;
		}
		for (final ProbeMode mode : ProbeMode.values()) {
			try {
				return mode.getProbeZeroInstance().newProbeArray(dataObject);
			} catch (final IllegalArgumentException e) {
				continue;
			}
		}
		throw new IllegalArgumentException(
				"Unable to locate concrete probe type to construct for argument type "
						+ dataObject.getClass().getName());
	}

	/**
	 * Read an {@code IProbeArray} instance
	 * 
	 * @param input
	 *            the input source
	 * @return the probe array of the type and value read
	 * @throws IOException
	 *             on any failure to read
	 */
	public static IProbeArray<?> read(final CompactDataInput input)
			throws IOException {
		final byte type = input.readByte();
		final IProbeArray<?> zeroInstance = ProbeMode.getZeroInstance(type);
		if (zeroInstance == null) {
			throw new IOException("Unrecognized probe type (" + (int) type
					+ ") in data stream");
		}
		return zeroInstance.read(input);
	}

	// InstrSupport
	/**
	 * Return the class name in 'slash' format that stores coverage information
	 * for the probe array type configured.
	 * 
	 * @return the class name in 'slash' format.
	 */
	public static String getDatafieldClass() {
		return getProbeZeroInstance().getDatafieldClass();
	}

	/**
	 * Return the data type of the field that stores coverage information for
	 * the probe array type configured.
	 *
	 * @return the data type of the field that stores coverage information
	 */
	public static String getDatafieldDesc() {
		return getProbeZeroInstance().getDatafieldDesc();
	}

	/**
	 * Returns the descriptor of the probe initialization method for the probe
	 * type configured.
	 *
	 * @return the descriptor of the probe initialization method.
	 */
	public static String getInitMethodDesc() {
		return getProbeZeroInstance().getInitMethodDesc();
	}

	// FieldProbeArrayStrategy
	/**
	 * Generates the byte code to initialize the static coverage data field
	 * within the class for the probe type configured.
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
	public static void createInitMethod(final ClassVisitor cv,
			final long classId, final String className,
			final boolean withFrames,
			final IExecutionDataAccessorGenerator accessorGenerator,
			final int probeCount) {
		getProbeZeroInstance().createInitMethod(cv, classId, className,
				withFrames, accessorGenerator, probeCount);
	}

	// ProbeInserter
	/**
	 * Generates the byte code to perform an increment on a probe for the probe
	 * type configured.
	 * 
	 * @param mv
	 *            the method visitor of the method "poked"
	 * @param variable
	 *            position of inserted variable
	 * @param id
	 *            the id of the probe to update
	 */
	public static void insertProbe(final MethodVisitor mv, final int variable,
			final int id) {
		getProbeZeroInstance().insertProbe(mv, variable, id);
	}

	/**
	 * Returns the stack size required by an increment probe operation of the
	 * configured type.
	 * 
	 * @return the stack size required by an increment probe operation
	 */
	public static int incrementProbeStackSize() {
		return getProbeZeroInstance().incrementProbeStackSize();
	}

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
	public static MethodVisitor addProbeAdvisor(final MethodVisitor mv,
			final int access, final String name, final String desc) {
		return getProbeZeroInstance().addProbeAdvisor(mv, access, name, desc);
	}

}
