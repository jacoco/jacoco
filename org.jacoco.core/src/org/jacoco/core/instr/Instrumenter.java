/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.internal.ContentTypeDetector;
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
	 */
	public byte[] instrument(final InputStream input) throws IOException {
		return instrument(new ClassReader(input));
	}

	/**
	 * Creates a instrumented version of the given class file.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param output
	 *            stream to write the instrumented version of the class to
	 * @throws IOException
	 *             if reading data from the stream fails
	 */
	public void instrument(final InputStream input, final OutputStream output)
			throws IOException {
		output.write(instrument(new ClassReader(input)));
	}

	/**
	 * Creates a instrumented version of the given archive, i.e. with all class
	 * files contained in this archive instrumented. Contained resources which
	 * are no class files or archive files are copied as is.
	 * 
	 * @param input
	 *            stream to read archive from
	 * @param output
	 *            stream to write the instrumented version of the class to
	 * @return number of instrumented classes
	 * @throws IOException
	 *             if reading data from the stream fails
	 */
	public int instrumentArchive(final InputStream input,
			final OutputStream output) throws IOException {
		final ZipInputStream zipin = new ZipInputStream(input);
		final ZipOutputStream zipout = new ZipOutputStream(output);
		ZipEntry entry;
		int count = 0;
		while ((entry = zipin.getNextEntry()) != null) {
			zipout.putNextEntry(new ZipEntry(entry.getName()));
			count += instrumentAll(zipin, zipout);
			zipout.closeEntry();
		}
		zipout.finish();
		return count;
	}

	/**
	 * Creates a instrumented version of the given resource depending on its
	 * type. Class files and the content of archive files are instrumented. All
	 * other files are copied without modification.
	 * 
	 * @param input
	 *            stream to contents from
	 * @param output
	 *            stream to write the instrumented version of the contents
	 * @return number of instrumented classes
	 * @throws IOException
	 *             if reading data from the stream fails
	 */
	public int instrumentAll(final InputStream input, final OutputStream output)
			throws IOException {
		final ContentTypeDetector detector = new ContentTypeDetector(input);
		switch (detector.getType()) {
		case ContentTypeDetector.CLASSFILE:
			instrument(detector.getInputStream(), output);
			return 1;
		case ContentTypeDetector.ZIPFILE:
			return instrumentArchive(detector.getInputStream(), output);
		default:
			final byte[] buffer = new byte[1024];
			int len;
			while ((len = detector.getInputStream().read(buffer)) != -1) {
				output.write(buffer, 0, len);
			}
			return 0;
		}
	}
}
