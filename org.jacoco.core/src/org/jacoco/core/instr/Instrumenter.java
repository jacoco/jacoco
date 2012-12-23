/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * Several APIs to instrument Java class definitions for coverage tracing.
 */
public class Instrumenter {

	private final IExecutionDataAccessorGenerator accessGenerator;

	/**
	 * Creates a new instance based on the given runtime.
	 * 
	 * @param runtime
	 *            runtime used by the instrumented classes
	 */
	public Instrumenter(final IExecutionDataAccessorGenerator runtime) {
		this.accessGenerator = runtime;
	}

	/**
	 * Creates a ASM adapter for a class with the given id.
	 * 
	 * @param classid
	 *            id of the class calculated with {@link CRC64}
	 * @param cv
	 *            next class visitor in the chain
	 * @return new visitor to write class definition to
	 */
	private ClassVisitor createInstrumentingVisitor(final long classid,
			final ClassVisitor cv) {
		return new ClassProbesAdapter(new ClassInstrumenter(classid,
				accessGenerator, cv));
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
		final ClassWriter writer = new ClassWriter(reader, 0);
		final ClassVisitor visitor = createInstrumentingVisitor(
				CRC64.checksum(reader.b), writer);
		reader.accept(visitor, ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}

	/**
	 * Creates a instrumented version of the given class if possible.
	 * 
	 * @param buffer
	 *            definition of the class
	 * @return instrumented definition
	 * 
	 */
	public byte[] instrument(final byte[] buffer) {
		return instrument(new ClassReader(buffer));
	}

	/**
	 * Creates a instrumented version of the given class if possible.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @return instrumented definition
	 * @throws IOException
	 *             if reading data from the stream fails
	 * 
	 */
	public byte[] instrument(final InputStream input) throws IOException {
		return instrument(new ClassReader(input));
	}

}
