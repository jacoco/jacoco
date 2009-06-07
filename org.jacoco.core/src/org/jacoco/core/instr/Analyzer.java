/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jacoco.core.data.IClassStructureOutput;
import org.jacoco.core.data.IStructureOutput;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Several APIs to analyze class structures.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Analyzer {

	private final IStructureOutput structureOutput;

	/**
	 * Creates a new analyzer reporting to the given output.
	 * 
	 * @param structureOutput
	 *            the output instance that will receive all structure data
	 */
	public Analyzer(IStructureOutput structureOutput) {
		this.structureOutput = structureOutput;
	}

	/**
	 * Creates an ASM class visitor for analysis.
	 * 
	 * @param classid
	 *            id of the class calculated with {@link CRC64}
	 * @param classname
	 *            VM name of the class
	 * @param bundle
	 *            optional bundle this class belongs to
	 * @return ASM visitor to write class definition to
	 */
	public ClassVisitor createAnalyzingVisitor(long classid, String classname,
			String bundle) {
		final IClassStructureOutput classStructure = structureOutput
				.classStructure(classid, classname, bundle);
		return new ClassAnalyzer(classStructure);
	}

	/**
	 * Analyzes the class given as a ASM reader.
	 * 
	 * @param reader
	 *            reader with class definitions
	 * @param bundle
	 *            optional bundle this class belongs to
	 */
	public void analyze(ClassReader reader, String bundle) {
		if ((reader.getAccess() & Opcodes.ACC_INTERFACE) != 0) {
			return;
		}

		final ClassVisitor visitor = createAnalyzingVisitor(CRC64
				.checksum(reader.b), reader.getClassName(), bundle);
		reader.accept(visitor, 0);
	}

	/**
	 * Analyzes the class definition from a given in-memory buffer.
	 * 
	 * @param buffer
	 *            class definitions
	 * @param bundle
	 *            optional bundle this class belongs to
	 */
	public void analyze(byte[] buffer, String bundle) {
		analyze(new ClassReader(buffer), bundle);
	}

	/**
	 * Analyzes the class definition from a given input stream.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param bundle
	 *            optional bundle this class belongs to
	 * @throws IOException
	 */
	public void analyze(InputStream input, String bundle) throws IOException {
		analyze(new ClassReader(input), bundle);
	}

	/**
	 * Analyzes the class definition contained in a given file.
	 * 
	 * @param file
	 *            class file
	 * @param bundle
	 *            optional bundle this class belongs to
	 * @throws IOException
	 */
	public void analyze(File file, String bundle) throws IOException {
		final InputStream in = new FileInputStream(file);
		analyze(new ClassReader(in), bundle);
		in.close();
	}

	/**
	 * Analyzes all class files contained in the given directory and its
	 * children.
	 * 
	 * @param directory
	 *            folder to look for class files
	 * @param bundle
	 *            optional bundle all the classes belong to
	 * @throws IOException
	 */
	public void analyzeAll(File directory, String bundle) throws IOException {
		for (final File f : directory.listFiles()) {
			if (f.isDirectory()) {
				analyzeAll(directory, bundle);
				continue;
			}
			if (f.getName().endsWith(".class")) {
				analyze(f, bundle);
			}
		}
	}

	/**
	 * Analyzes all class files contained in a JAR file.
	 * 
	 * @param input
	 *            stream to read the JAR file from
	 * @param bundle
	 *            optional bundle all the classes belong to
	 * @throws IOException
	 */
	public void analyzeJAR(InputStream input, String bundle) throws IOException {
		final ZipInputStream zip = new ZipInputStream(input);
		while (true) {
			final ZipEntry entry = zip.getNextEntry();
			if (entry == null) {
				break;
			}
			if (entry.getName().endsWith(".class")) {
				analyze(zip, bundle);
			}
		}
	}

	/**
	 * Analyzes all class files contained in a JAR file.
	 * 
	 * @param jarfile
	 *            JAR file
	 * @param bundle
	 *            optional bundle all the classes belong to
	 * @throws IOException
	 */
	public void analyzeJAR(File jarfile, String bundle) throws IOException {
		final InputStream in = new FileInputStream(jarfile);
		analyzeJAR(in, bundle);
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
	 * @param bundle
	 *            optional bundle all the classes belong to
	 * @throws IOException
	 */
	public void analyzePath(String path, File basedir, String bundle)
			throws IOException {
		final StringTokenizer tokenizer = new StringTokenizer(path,
				File.pathSeparator);
		while (tokenizer.hasMoreTokens()) {
			final File entry = new File(basedir, tokenizer.nextToken());
			if (entry.isDirectory()) {
				analyzeAll(entry, bundle);
				continue;
			}
			if (entry.isFile() && entry.getName().endsWith(".jar")) {
				analyzeJAR(entry, bundle);
			}
		}
	}

}
