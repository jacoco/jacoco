/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.internal.instr.ProbeArrayStrategyFactory;
import org.jacoco.core.internal.instr.SignatureRemover;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * Several APIs to instrument Java class definitions for coverage tracing.
 */
public class Instrumenter {

	private final IExecutionDataAccessorGenerator accessorGenerator;

	private final SignatureRemover signatureRemover;

	/**
	 * Creates a new instance based on the given runtime.
	 * 
	 * @param runtime
	 *            runtime used by the instrumented classes
	 */
	public Instrumenter(final IExecutionDataAccessorGenerator runtime) {
		this.accessorGenerator = runtime;
		this.signatureRemover = new SignatureRemover();
	}

	/**
	 * Determines whether signatures should be removed from JAR files. This is
	 * typically necessary as instrumentation modifies the class files and
	 * therefore invalidates existing JAR signatures. Default is
	 * <code>true</code>.
	 * 
	 * @param flag
	 *            <code>true</code> if signatures should be removed
	 */
	public void setRemoveSignatures(final boolean flag) {
		signatureRemover.setActive(flag);
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
		final IProbeArrayStrategy strategy = ProbeArrayStrategyFactory
				.createFor(reader, accessorGenerator);
		final ClassVisitor visitor = new ClassProbesAdapter(
				new ClassInstrumenter(strategy, writer), true);
		reader.accept(visitor, ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
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
	 *             if the class can't be analyzed
	 */
	public byte[] instrument(final byte[] buffer, final String name)
			throws IOException {
		try {
			return instrument(new ClassReader(buffer));
		} catch (final RuntimeException e) {
			throw instrumentError(name, e);
		}
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
		try {
			return instrument(new ClassReader(input));
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
		try {
			output.write(instrument(new ClassReader(input)));
		} catch (final RuntimeException e) {
			throw instrumentError(name, e);
		}
	}

	private IOException instrumentError(final String name,
			final RuntimeException cause) {
		final IOException ex = new IOException(String.format(
				"Error while instrumenting class %s.", name));
		ex.initCause(cause);
		return ex;
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
	 * @param name
	 *            a name used for exception messages
	 * @return number of instrumented classes
	 * @throws IOException
	 *             if reading data from the stream fails or a class can't be
	 *             instrumented
	 */
	public int instrumentAll(final InputStream input,
			final OutputStream output, final String name) throws IOException {
		final ContentTypeDetector detector = new ContentTypeDetector(input);
		switch (detector.getType()) {
		case ContentTypeDetector.CLASSFILE:
			instrument(detector.getInputStream(), output, name);
			return 1;
		case ContentTypeDetector.ZIPFILE:
			return instrumentZip(detector.getInputStream(), output, name);
		case ContentTypeDetector.GZFILE:
			return instrumentGzip(detector.getInputStream(), output, name);
		case ContentTypeDetector.PACK200FILE:
			return instrumentPack200(detector.getInputStream(), output, name);
		default:
			copy(detector.getInputStream(), output);
			return 0;
		}
	}

	private int instrumentZip(final InputStream input,
			final OutputStream output, final String name) throws IOException {
		final ZipInputStream zipin = new ZipInputStream(input);
		final ZipOutputStream zipout = new ZipOutputStream(output);
		ZipEntry entry;
		int count = 0;
		while ((entry = zipin.getNextEntry()) != null) {
			final String entryName = entry.getName();
			if (signatureRemover.removeEntry(entryName)) {
				continue;
			}

			zipout.putNextEntry(new ZipEntry(entryName));
			if (!signatureRemover.filterEntry(entryName, zipin, zipout)) {
				count += instrumentAll(zipin, zipout, name + "@" + entryName);
			}
			zipout.closeEntry();
		}
		zipout.finish();
		return count;
	}

	private int instrumentGzip(final InputStream input,
			final OutputStream output, final String name) throws IOException {
		final GZIPOutputStream gzout = new GZIPOutputStream(output);
		final int count = instrumentAll(new GZIPInputStream(input), gzout, name);
		gzout.finish();
		return count;
	}

	private int instrumentPack200(final InputStream input,
			final OutputStream output, final String name) throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final int count = instrumentAll(Pack200Streams.unpack(input), buffer,
				name);
		Pack200Streams.pack(buffer.toByteArray(), output);
		return count;
	}

	private void copy(final InputStream input, final OutputStream output)
			throws IOException {
		final byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) != -1) {
			output.write(buffer, 0, len);
		}
	}

}
