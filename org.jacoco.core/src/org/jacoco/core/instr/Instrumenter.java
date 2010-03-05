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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.instr;

import java.io.IOException;
import java.io.InputStream;

import org.jacoco.core.runtime.IRuntime;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * Several APIs to instrument Java class definitions for coverage tracing.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Instrumenter {

	private final IRuntime runtime;

	/**
	 * Creates a new instance based on the given runtime.
	 * 
	 * @param runtime
	 *            runtime used by the instrumented classes
	 */
	public Instrumenter(final IRuntime runtime) {
		this.runtime = runtime;
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
	public ClassVisitor createInstrumentingVisitor(final long classid,
			final ClassVisitor cv) {
		return new ClassInstrumenter(classid, runtime, cv);
	}

	/**
	 * Creates a instrumented version of the given class if possible.
	 * 
	 * @param reader
	 *            definition of the class as ASM reader
	 * @return instrumented definition or <code>null</code>
	 * 
	 */
	public byte[] instrument(final ClassReader reader) {
		final ClassWriter writer = new ClassWriter(reader, 0);
		final ClassVisitor visitor = createInstrumentingVisitor(CRC64
				.checksum(reader.b), writer);
		reader.accept(visitor, ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}

	/**
	 * Creates a instrumented version of the given class if possible.
	 * 
	 * @param buffer
	 *            definition of the class
	 * @return instrumented definition or <code>null</code>
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
	 * @return instrumented definition or <code>null</code>
	 * @throws IOException
	 *             if reading data from the stream fails
	 * 
	 */
	public byte[] instrument(final InputStream input) throws IOException {
		return instrument(new ClassReader(input));
	}

}
