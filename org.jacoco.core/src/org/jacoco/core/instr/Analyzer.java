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

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jacoco.core.data.IClassStructureVisitor;
import org.jacoco.core.data.IStructureVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * Several APIs to analyze class structures.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Analyzer {

	private final IStructureVisitor structureVisitor;

	/**
	 * Creates a new analyzer reporting to the given output.
	 * 
	 * @param structureVisitor
	 *            the output instance that will receive all structure data
	 */
	public Analyzer(final IStructureVisitor structureVisitor) {
		this.structureVisitor = structureVisitor;
	}

	/**
	 * Creates an ASM class visitor for analysis.
	 * 
	 * @param classid
	 *            id of the class calculated with {@link CRC64}
	 * @return ASM visitor to write class definition to
	 */
	public ClassVisitor createAnalyzingVisitor(final long classid) {
		final IClassStructureVisitor classStructure = structureVisitor
				.visitClassStructure(classid);
		return new ClassAnalyzer(classStructure);
	}

	/**
	 * Analyzes the class given as a ASM reader.
	 * 
	 * @param reader
	 *            reader with class definitions
	 */
	public void analyze(final ClassReader reader) {
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
	public void analyze(final byte[] buffer) {
		analyze(new ClassReader(buffer));
	}

	/**
	 * Analyzes the class definition from a given input stream.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @throws IOException
	 */
	public void analyze(final InputStream input) throws IOException {
		analyze(new ClassReader(input));
	}

	/**
	 * Analyzes the class definition contained in a given file.
	 * 
	 * @param file
	 *            class file
	 * @throws IOException
	 */
	public void analyze(final File file) throws IOException {
		final InputStream in = new FileInputStream(file);
		analyze(new ClassReader(in));
		in.close();
	}

	/**
	 * Analyzes all class files contained in the given directory and its
	 * children.
	 * 
	 * @param directory
	 *            folder to look for class files
	 * @throws IOException
	 *             thrown if the given file object does not represent a readable
	 *             directory
	 */
	public void analyzeAll(final File directory) throws IOException {
		final File[] files = directory.listFiles();
		if (files == null) {
			throw new IOException(format("Can't read directory %s.", directory));
		}
		for (final File f : files) {
			if (f.isDirectory()) {
				analyzeAll(f);
				continue;
			}
			if (f.getName().endsWith(".class")) {
				analyze(f);
			}
		}
	}

	/**
	 * Analyzes all class files contained in a JAR file.
	 * 
	 * @param input
	 *            stream to read the JAR file from
	 * @throws IOException
	 */
	public void analyzeJAR(final InputStream input) throws IOException {
		final ZipInputStream zip = new ZipInputStream(input);
		while (true) {
			final ZipEntry entry = zip.getNextEntry();
			if (entry == null) {
				break;
			}
			if (entry.getName().endsWith(".class")) {
				analyze(zip);
			}
		}
	}

	/**
	 * Analyzes all class files contained in a JAR file.
	 * 
	 * @param jarfile
	 *            JAR file
	 * @throws IOException
	 */
	public void analyzeJAR(final File jarfile) throws IOException {
		final InputStream in = new FileInputStream(jarfile);
		analyzeJAR(in);
		in.close();
	}

	/**
	 * Analyzes all class from the given class path.
	 * 
	 * @param path
	 *            path definition
	 * @param basedir
	 *            optional base directory, if <code>null</code> the current
	 *            working directory is used as the base for relative path
	 *            entries
	 * @throws IOException
	 */
	public void analyzePath(final String path, final File basedir)
			throws IOException {
		final StringTokenizer tokenizer = new StringTokenizer(path,
				File.pathSeparator);
		while (tokenizer.hasMoreTokens()) {
			final File entry = new File(basedir, tokenizer.nextToken());
			if (entry.isDirectory()) {
				analyzeAll(entry);
				continue;
			}
			if (entry.isFile() && entry.getName().endsWith(".jar")) {
				analyzeJAR(entry);
			}
		}
	}

}
