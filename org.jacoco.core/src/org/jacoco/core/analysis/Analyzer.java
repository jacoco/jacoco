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
import org.jacoco.core.data.IProbes;
import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.analysis.ClassAnalyzer;
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
public class Analyzer implements IAnalyzer {

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
		final IProbes probes;
		final boolean noMatch;
		if (data == null) {
			probes = null;
			noMatch = executionData.contains(className);
		} else {
			probes = data.getProbes();
			noMatch = false;
		}
		final ClassAnalyzer analyzer = new ClassAnalyzer(classid, noMatch,
				probes, stringPool) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				coverageVisitor.visitCoverage(getCoverage());
			}
		};
		return new ClassProbesAdapter(analyzer, false);
	}

	public void analyzeClass(final ClassReader reader) {
		final ClassVisitor visitor = createAnalyzingVisitor(
				CRC64.checksum(reader.b), reader.getClassName());
		reader.accept(visitor, 0);
	}

	public void analyzeClass(final byte[] buffer, final String name)
			throws IOException {
		try {
			analyzeClass(new ClassReader(buffer));
		} catch (final RuntimeException cause) {
			throw analyzerError(name, cause);
		}
	}

	public void analyzeClass(final InputStream input, final String name)
			throws IOException {
		try {
			analyzeClass(new ClassReader(input));
		} catch (final RuntimeException e) {
			throw analyzerError(name, e);
		}
	}

	private IOException analyzerError(final String name,
			final RuntimeException cause) {
		final IOException ex = new IOException(String.format(
				"Error while analyzing class %s.", name));
		ex.initCause(cause);
		return ex;
	}

	public int analyzeAll(final InputStream input, final String name)
			throws IOException {
		final ContentTypeDetector detector = new ContentTypeDetector(input);
		switch (detector.getType()) {
		case ContentTypeDetector.CLASSFILE:
			analyzeClass(detector.getInputStream(), name);
			return 1;
		case ContentTypeDetector.ZIPFILE:
			return analyzeZip(detector.getInputStream(), name);
		case ContentTypeDetector.GZFILE:
			return analyzeGzip(detector.getInputStream(), name);
		case ContentTypeDetector.PACK200FILE:
			return analyzePack200(detector.getInputStream(), name);
		default:
			return 0;
		}
	}

	public int analyzeAll(final File file) throws IOException {
		int count = 0;
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				count += analyzeAll(f);
			}
		} else {
			final InputStream in = new FileInputStream(file);
			try {
				count += analyzeAll(in, file.getPath());
			} finally {
				in.close();
			}
		}
		return count;
	}

	public int analyzeAll(final String path, final File basedir)
			throws IOException {
		int count = 0;
		final StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		while (st.hasMoreTokens()) {
			count += analyzeAll(new File(basedir, st.nextToken()));
		}
		return count;
	}

	private int analyzeZip(final InputStream input, final String name)
			throws IOException {
		final ZipInputStream zip = new ZipInputStream(input);
		ZipEntry entry;
		int count = 0;
		while ((entry = zip.getNextEntry()) != null) {
			count += analyzeAll(zip, name + "@" + entry.getName());
		}
		return count;
	}

	private int analyzeGzip(final InputStream input, final String name)
			throws IOException {
		return analyzeAll(new GZIPInputStream(input), name);
	}

	private int analyzePack200(final InputStream input, final String name)
			throws IOException {
		return analyzeAll(Pack200Streams.unpack(input), name);
	}

}
