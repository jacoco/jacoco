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
package org.jacoco.core.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.Java9Support;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.analysis.ClassAnalyzer;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.StringPool;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * An {@link Analyzer} instance processes a set of Java class files and
 * calculates coverage data for them. For each class file the result is reported
 * to a given {@link ICoverageVisitor} instance. In addition the
 * {@link Analyzer} requires a {@link ExecutionDataStore} instance that holds
 * the execution data for the classes to analyze. The {@link Analyzer} offers
 * several methods to analyze classes from a variety of sources.
 */
public class Analyzer {

	private final ExecutionDataStore executionData;

	private final ICoverageVisitor coverageVisitor;

	private final StringPool stringPool;

	/**
	 * Creates a new analyzer reporting to the given output.
	 * 
	 * @param executionData
	 *            execution data
	 * @param coverageVisitor
	 *            the output instance that will coverage data for every analyzed
	 *            class
	 */
	public Analyzer(final ExecutionDataStore executionData,
			final ICoverageVisitor coverageVisitor) {
		this.executionData = executionData;
		this.coverageVisitor = coverageVisitor;
		this.stringPool = new StringPool();
	}

	/**
	 * Creates an ASM class visitor for analysis.
	 * 
	 * @param classid
	 *            id of the class calculated with {@link CRC64}
	 * @param className
	 *            VM name of the class
	 * @return ASM visitor to write class definition to
	 */
	private ClassVisitor createAnalyzingVisitor(final long classid,
			final String className) {
		final ExecutionData data = executionData.get(classid);
		final boolean[] probes;
		final boolean noMatch;
		if (data == null) {
			probes = null;
			noMatch = executionData.contains(className);
		} else {
			probes = data.getProbes();
			noMatch = false;
		}
		final ClassCoverageImpl coverage = new ClassCoverageImpl(className,
				classid, noMatch);
		final ClassAnalyzer analyzer = new ClassAnalyzer(coverage, probes,
				stringPool) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				coverageVisitor.visitCoverage(coverage);
			}
		};
		return new ClassProbesAdapter(analyzer, false);
	}

	/**
	 * Analyzes the class given as a ASM reader.
	 * 
	 * @param reader
	 *            reader with class definitions
	 */
	public void analyzeClass(final ClassReader reader) {
		final ClassVisitor visitor = createAnalyzingVisitor(
				CRC64.checksum(reader.b), reader.getClassName());
		reader.accept(visitor, 0);
	}

	/**
	 * Analyzes the class definition from a given in-memory buffer.
	 * 
	 * @param buffer
	 *            class definitions
	 * @param location
	 *            a location description used for exception messages
	 * @throws IOException
	 *             if the class can't be analyzed
	 */
	public void analyzeClass(final byte[] buffer, final String location)
			throws IOException {
		try {
			analyzeClass(asClassReader(buffer, location));
		} catch (ArrayIndexOutOfBoundsException e) {
			throw analyzerError(location, e);
		}
	}

	/**
	 * Analyzes the class definition from a given input stream.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param location
	 *            a location description used for exception messages
	 * @throws IOException
	 *             if the stream can't be read or the class can't be analyzed
	 */
	public void analyzeClass(final InputStream input, final String location)
			throws IOException {
		analyzeClass(asClassReader(input, location));
	}

	/**
	 * Creates a ClassReader for the given buffer
	 * @param buffer binary representation of the class
	 * @param location where the class came from (for error messages)
	 * @return ClassReader
	 * @throws IOException if input cannot be decoded to a valid ClassReader
	 */
	public ClassReader asClassReader(final byte[] buffer, final String location)
			throws IOException {
		try {
			return new ClassReader(Java9Support.downgradeIfRequired(buffer));
		} catch (final RuntimeException cause) {
			throw analyzerError(location, cause);
		}
	}

	/**
	 * Creates a ClassReader for the given buffer
	 * @param input binary representation of the class
	 * @param location where the class came from (for error messages)
	 * @return ClassReader
	 * @throws IOException if input cannot be decoded to a valid ClassReader
	 */
	public ClassReader asClassReader(final InputStream input, final String location)
			throws IOException {
		try {
			return asClassReader(Java9Support.readFully(input), location);
		} catch (final RuntimeException e) {
			throw analyzerError(location, e);
		}
	}


	private IOException analyzerError(final String location,
			final Exception cause) {
		final IOException ex = new IOException(String.format(
				"Error while analyzing %s.", location));
		ex.initCause(cause);
		return ex;
	}

}
