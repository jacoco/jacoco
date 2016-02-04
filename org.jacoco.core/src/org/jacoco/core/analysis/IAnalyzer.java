/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

/**
 * An {@link IAnalyzer} implementation processes a set of Java class files and
 * calculates coverage data for them. For each class file the result is reported
 * to a given {@link ICoverageVisitor} instance. Each {@link IAnalyzer}
 * implementation requires some sort of data store object instance that holds
 * the data for the classes to analyze. An {@link IAnalyzer} offers several
 * methods to analyze classes from a variety of sources.
 */
public interface IAnalyzer {

	/**
	 * Analyzes the class given as a ASM reader.
	 * 
	 * @param reader
	 *            reader with class definitions
	 * @throws IOException
	 *             if the class can't be analyzed
	 */
	public abstract void analyzeClass(ClassReader reader) throws IOException;

	/**
	 * Analyzes the class definition from a given in-memory buffer.
	 * 
	 * @param buffer
	 *            class definitions
	 * @param name
	 *            a name used for exception messages
	 * @throws IOException
	 *             if the class can't be analyzed
	 */
	public abstract void analyzeClass(byte[] buffer, String name)
			throws IOException;

	/**
	 * Analyzes the class definition from a given input stream.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param name
	 *            a name used for exception messages
	 * @throws IOException
	 *             if the stream can't be read or the class can't be analyzed
	 */
	public abstract void analyzeClass(InputStream input, String name)
			throws IOException;

	/**
	 * Analyzes all classes found in the given input stream. The input stream
	 * may either represent a single class file, a ZIP archive, a Pack200
	 * archive or a gzip stream that is searched recursively for class files.
	 * All other content types are ignored.
	 * 
	 * @param input
	 *            input data
	 * @param name
	 *            a name used for exception messages
	 * @return number of class files found
	 * @throws IOException
	 *             if the stream can't be read or a class can't be analyzed
	 */
	public abstract int analyzeAll(InputStream input, String name)
			throws IOException;

	/**
	 * Analyzes all class files contained in the given file or folder. Class
	 * files as well as ZIP files are considered. Folders are searched
	 * recursively.
	 * 
	 * @param file
	 *            file or folder to look for class files
	 * @return number of class files found
	 * @throws IOException
	 *             if the file can't be read or a class can't be analyzed
	 */
	public abstract int analyzeAll(File file) throws IOException;

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
	 *             if a file can't be read or a class can't be analyzed
	 */
	public abstract int analyzeAll(String path, File basedir)
			throws IOException;
}