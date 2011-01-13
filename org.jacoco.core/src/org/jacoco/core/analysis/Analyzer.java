/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jacoco.core.data.CRC64;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.ClassAnalyzer;
import org.jacoco.core.internal.analysis.ContentTypeDetector;
import org.jacoco.core.internal.analysis.StringPool;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * Several APIs to analyze class structures.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class Analyzer {

	private final ExecutionDataStore executionData;

	private final ICoverageVisitor structureVisitor;

	private final StringPool stringPool;

	/**
	 * Creates a new analyzer reporting to the given output.
	 * 
	 * @param executionData
	 *            execution data
	 * @param structureVisitor
	 *            the output instance that will receive all structure data
	 */
	public Analyzer(final ExecutionDataStore executionData,
			final ICoverageVisitor structureVisitor) {
		this.executionData = executionData;
		this.structureVisitor = structureVisitor;
		this.stringPool = new StringPool();
	}

	/**
	 * Creates an ASM class visitor for analysis.
	 * 
	 * @param classid
	 *            id of the class calculated with {@link CRC64}
	 * @return ASM visitor to write class definition to
	 */
	public ClassVisitor createAnalyzingVisitor(final long classid) {
		final ExecutionData data = executionData.get(classid);
		final boolean[] classExec = data == null ? null : data.getData();
		final ClassAnalyzer analyzer = new ClassAnalyzer(classid, classExec,
				stringPool) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				structureVisitor.visitCoverage(getCoverage());
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
		final ClassVisitor visitor = createAnalyzingVisitor(CRC64
				.checksum(reader.b));
		reader.accept(visitor, 0);
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
		}
		return 0;
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
			count += analyzeAll(in);
			in.close();
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
