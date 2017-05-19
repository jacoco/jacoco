/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.instr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jacoco.core.internal.Java9Support;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.internal.instr.ProbeArrayStrategyFactory;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * Several APIs to instrument Java class definitions for coverage tracing.
 */
public class Instrumenter {

	private final IExecutionDataAccessorGenerator accessorGenerator;

	/**
	 * Creates a new instance based on the given runtime.
	 * 
	 * @param runtime
	 *            runtime used by the instrumented classes
	 */
	public Instrumenter(final IExecutionDataAccessorGenerator runtime) {
		this.accessorGenerator = runtime;
	}

	/**
	 * Creates a instrumented version of the given class if possible.
	 * 
	 * @param reader
	 *            definition of the class as ASM reader
	 * @return instrumented definition
	 * 
	 */
	public byte[] instrument(final ClassReader reader) {
		final ClassWriter writer = new ClassWriter(reader, 0) {
			@Override
			protected String getCommonSuperClass(final String type1,
					final String type2) {
				throw new IllegalStateException();
			}
		};
		final IProbeArrayStrategy strategy = ProbeArrayStrategyFactory
				.createFor(reader, accessorGenerator);
		final ClassVisitor visitor = new ClassProbesAdapter(
				new ClassInstrumenter(strategy, writer), true);
		reader.accept(visitor, ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}

	/**
	 * Creates a instrumented version of the given class if possible
	 * Also upgrades to Java9 if requested
	 *
	 * @param request
	 *            InstrumentationRequest giving the class to instrument and a
	 * @return instrumented definition
	 *
	 */
	public byte[] instrument(final InstrumentationRequest request) {
		byte[] bytes = instrument(request.getClassReader());
		if (request.getUpgradeToJava9()) {
			Java9Support.upgrade(bytes);
		}
		return bytes;
	}

	/**
	 * Creates a instrumented version of the given class if possible.
	 * 
	 * @param buffer
	 *            definition of the class
	 * @param name
	 *            a name used for exception messages
	 * @return instrumented definition
	 * @throws IOException
	 *             if the class can't be instrumented
	 */
	public byte[] instrument(final byte[] buffer, final String name)
			throws IOException {
		byte[] result;
		try {
			result = instrument(asClassReader(buffer, name));
		} catch (NullPointerException e) {
			throw instrumentError(name, e);
		}
		return result;
	}

	/**
	 * Creates a instrumented version of the given class if possible.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param name
	 *            a name used for exception messages
	 * @return instrumented definition
	 * @throws IOException
	 *             if reading data from the stream fails or the class can't be
	 *             instrumented
	 */
	public byte[] instrument(final InputStream input, final String name)
			throws IOException {
		return instrument(asClassReader(input, name));
	}

	/**
	 * Makes a ClassReader for the given input and name
	 * @param input data from which to make a ClassReader
	 * @param name name to include in any error message
	 * @return InstrumentationRequest containing the ClassReader
	 * @throws IOException in case of failure to read
	 */
	public InstrumentationRequest asClassReader(final InputStream input, final String name) throws IOException {
		final byte[] bytes;
		try {
			bytes = Java9Support.readFully(input);
		} catch (final IOException e) {
			throw instrumentError(name, e);
		}
		return asClassReader(bytes, name);
	}

	private InstrumentationRequest asClassReader(final byte[] buffer, final String name)
			throws IOException {
		try {
			if (Java9Support.isPatchRequired(buffer)) {
				return new InstrumentationRequest(new ClassReader(Java9Support.downgrade(buffer))).setUpgradeToJava9(true);
			} else {
				return new InstrumentationRequest(new ClassReader(buffer));
			}
		} catch (final RuntimeException e) {
			throw instrumentError(name, e);
		}
	}

	/**
	 * Creates a instrumented version of the given class file.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param output
	 *            stream to write the instrumented version of the class to
	 * @param name
	 *            a name used for exception messages
	 * @throws IOException
	 *             if reading data from the stream fails or the class can't be
	 *             instrumented
	 */
	public void instrument(final InputStream input, final OutputStream output,
			final String name) throws IOException {
		output.write(instrument(input, name));
	}

	public IOException instrumentError(final String name,
			final Exception cause) {
		final IOException ex = new IOException(
				String.format("Error while instrumenting %s.", name));
		ex.initCause(cause);
		return ex;
	}

}
