/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    Martin Hare Robertson - filters
 *    
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.ClassAnalyzer;
import org.jacoco.core.internal.analysis.ContentTypeDetector;
import org.jacoco.core.internal.analysis.StringPool;
import org.jacoco.core.internal.analysis.filters.CommentExclusionsCoverageFilter;
import org.jacoco.core.internal.analysis.filters.CompositeCoverageFilter;
import org.jacoco.core.internal.analysis.filters.EmptyConstructorCoverageFilter;
import org.jacoco.core.internal.analysis.filters.ICoverageFilterStatus.ICoverageFilter;
import org.jacoco.core.internal.analysis.filters.ImplicitEnumMethodsCoverageFilter;
import org.jacoco.core.internal.analysis.filters.SynchronizedExitCoverageFilter;
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

	private final ICoverageFilter coverageFilter;

	/**
	 * Creates a new analyzer reporting to the given output. This constructor
	 * uses a default filter which includes all coverage data.
	 * 
	 * @param executionData
	 *            execution data
	 * @param coverageVisitor
	 *            the output instance that will coverage data for every analyzed
	 *            class
	 */
	public Analyzer(final ExecutionDataStore executionData,
			final ICoverageVisitor coverageVisitor) {
		this(executionData, coverageVisitor, null);
	}

	/**
	 * Creates a new analyzer reporting to the given output.
	 * 
	 * @param executionData
	 *            execution data
	 * @param coverageVisitor
	 *            the output instance that will coverage data for every analyzed
	 *            class
	 * @param directivesParser
	 *            the parser for loading source directives or null if source
	 *            directives should be ignored
	 */
	public Analyzer(final ExecutionDataStore executionData,
			final ICoverageVisitor coverageVisitor,
			final IDirectivesParser directivesParser) {
		this.executionData = executionData;
		this.coverageVisitor = coverageVisitor;
		this.stringPool = new StringPool();

		final List<ICoverageFilter> filters = new ArrayList<ICoverageFilter>();
		filters.add(new ImplicitEnumMethodsCoverageFilter());
		filters.add(new EmptyConstructorCoverageFilter());
		filters.add(new SynchronizedExitCoverageFilter());
		if (directivesParser != null) {
			filters.add(new CommentExclusionsCoverageFilter(directivesParser));
		}

		this.coverageFilter = new CompositeCoverageFilter(filters);
	}

	/**
	 * Creates an ASM class visitor for analysis.
	 * 
	 * @param classid
	 *            id of the class calculated with {@link CRC64}
	 * @return ASM visitor to write class definition to
	 */
	private ClassVisitor createAnalyzingVisitor(final long classid) {
		final ExecutionData data = executionData.get(classid);
		final boolean[] classExec = data == null ? null : data.getData();
		final ClassAnalyzer analyzer = new ClassAnalyzer(classid, classExec,
				stringPool, coverageFilter) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				coverageVisitor.visitCoverage(getCoverage());
			}
		};
		return new ClassProbesAdapter(analyzer);
	}

	/**
	 * Analyzes the class given as a ASM reader.
	 * 
	 * @param reader
	 *            reader with class definitions
	 */
	public void analyzeClass(final ClassReader reader) {
		if (coverageFilter.includeClass(reader.getClassName())) {
			final ClassVisitor visitor = createAnalyzingVisitor(CRC64
					.checksum(reader.b));
			reader.accept(coverageFilter.visitClass(visitor), 0);
		}
	}

	/**
	 * Analyzes the class definition from a given in-memory buffer.
	 * 
	 * @param buffer
	 *            class definitions
	 */
	public void analyzeClass(final byte[] buffer) {
		analyzeClass(new ClassReader(buffer));
	}

	/**
	 * Analyzes the class definition from a given input stream.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @throws IOException
	 */
	public void analyzeClass(final InputStream input) throws IOException {
		analyzeClass(new ClassReader(input));
	}

	/**
	 * Analyzes all classes contained in the ZIP archive (jar, war, ear, etc.)
	 * given as an input stream. Contained archives are read recursively.
	 * 
	 * @param input
	 *            ZIP archive data
	 * @return number of class files found
	 * @throws IOException
	 */
	public int analyzeArchive(final InputStream input) throws IOException {
		final ZipInputStream zip = new ZipInputStream(input);
		int count = 0;
		while (true) {
			final ZipEntry entry = zip.getNextEntry();
			if (entry == null) {
				break;
			}
			count += analyzeAll(zip);
		}
		return count;
	}

	/**
	 * Analyzes all classes found in the given input stream. The input stream
	 * may either represent a single class file or a ZIP archive that is
	 * searched recursively for class files. All other content types are
	 * ignored.
	 * 
	 * @param input
	 *            input data
	 * @return number of class files found
	 * @throws IOException
	 */
	public int analyzeAll(final InputStream input) throws IOException {
		final ContentTypeDetector detector = new ContentTypeDetector(input);
		switch (detector.getType()) {
		case ContentTypeDetector.CLASSFILE:
			analyzeClass(detector.getInputStream());
			return 1;
		case ContentTypeDetector.ZIPFILE:
			return analyzeArchive(detector.getInputStream());
		default:
			return 0;
		}
	}

	/**
	 * Analyzes all class files contained in the given file or folder. Class
	 * files as well as ZIP files are considered. Folders are searched
	 * recursively.
	 * 
	 * @param file
	 *            file or folder to look for class files
	 * @return number of class files found
	 * @throws IOException
	 */
	public int analyzeAll(final File file) throws IOException {
		int count = 0;
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				count += analyzeAll(f);
			}
		} else {
			final InputStream in = new FileInputStream(file);
			try {
				count += analyzeAll(in);
			} finally {
				in.close();
			}
		}
		return count;
	}

	/**
	 * Analyzes all classes from the given class path. Directories containing
	 * class files as well as archive files are considered.
	 * 
	 * @param path
	 *            path definition
	 * @param basedir
	 *            optional base directory, if <code>null</code> the current
	 *            working directory is used as the base for relative path
	 *            entries
	 * @return number of class files found
	 * @throws IOException
	 */
	public int analyzeAll(final String path, final File basedir)
			throws IOException {
		int count = 0;
		final StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		while (st.hasMoreTokens()) {
			count += analyzeAll(new File(basedir, st.nextToken()));
		}
		return count;
	}

}
