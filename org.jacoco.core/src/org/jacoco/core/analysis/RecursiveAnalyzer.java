/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeffry Gaston - initial API and implementation
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
import org.jacoco.core.matcher.ClassnameMatcher;
import org.jacoco.core.matcher.Matcher;
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
public class RecursiveAnalyzer {

  private Analyzer analyzer;
  private Matcher<ClassReader> analysisPredicate;


  /**
   * Creates a new analyzer reporting to the given output.
   *
   * @param executionData
   *            execution data
   * @param coverageVisitor
   *            the output instance that will coverage data for every analyzed
   *            class
   */
  public RecursiveAnalyzer(final ExecutionDataStore executionData,
      final ICoverageVisitor coverageVisitor) {
    analyzer = new Analyzer(executionData, coverageVisitor);
    this.analysisPredicate = new ClassnameMatcher();
  }

  /**
   * Creates a new analyzer reporting to the given output.
   *
   * @param executionData
   *            execution data
   * @param coverageVisitor
   *            the output instance that will coverage data for every analyzed
   *            class
   * @param analysisPredicate
   *            filters which classes will be included in the analysis
   */
  public RecursiveAnalyzer(final ExecutionDataStore executionData,
      final ICoverageVisitor coverageVisitor, Matcher<ClassReader> analysisPredicate) {
    analyzer = new Analyzer(executionData, coverageVisitor);
    this.analysisPredicate = analysisPredicate;
  }


  /**
   * Analyzes the class given as a ASM reader.
   *
   * @param reader
   *            reader with class definitions
   */
  public void analyzeClassIfNotExcluded(final ClassReader reader, String location) throws IOException {
    boolean included;
    try {
      included = analysisPredicate.matches(reader);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw analyzerError(location, e);
    }
    if (included) {
      analyzer.analyzeClass(reader);
    }
  }
  private IOException analyzerError(final String location,
      final Exception cause) {
    final IOException ex = new IOException(String.format(
        "Error while analyzing %s.", location));
    ex.initCause(cause);
    return ex;
  }

  /**
   * Analyzes all classes found in the given input stream. The input stream
   * may either represent a single class file, a ZIP archive, a Pack200
   * archive or a gzip stream that is searched recursively for class files.
   * All other content types are ignored.
   *
   * @param input
   *            input data
   * @param location
   *            a location description used for exception messages
   * @return number of class files found
   * @throws IOException
   *             if the stream can't be read or a class can't be analyzed
   */
  public int analyzeAll(final InputStream input, final String location)
      throws IOException {
    final ContentTypeDetector detector;
    try {
      detector = new ContentTypeDetector(input);
    } catch (IOException e) {
      throw analyzerError(location, e);
    }
    switch (detector.getType()) {
      case ContentTypeDetector.CLASSFILE:
        analyzeClassIfNotExcluded(analyzer.asClassReader(detector.getInputStream(), location), location);
        return 1;
      case ContentTypeDetector.ZIPFILE:
        return analyzeZip(detector.getInputStream(), location);
      case ContentTypeDetector.GZFILE:
        return analyzeGzip(detector.getInputStream(), location);
      case ContentTypeDetector.PACK200FILE:
        return analyzePack200(detector.getInputStream(), location);
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
   *             if the file can't be read or a class can't be analyzed
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
        count += analyzeAll(in, file.getPath());
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
   *             if a file can't be read or a class can't be analyzed
   */
  public int analyzeAll(final String path, final File basedir)
      throws IOException {
    int count = 0;
    final StringTokenizer st = new StringTokenizer(path,
        File.pathSeparator);
    while (st.hasMoreTokens()) {
      count += analyzeAll(new File(basedir, st.nextToken()));
    }
    return count;
  }

  private int analyzeZip(final InputStream input, final String location)
      throws IOException {
    final ZipInputStream zip = new ZipInputStream(input);
    ZipEntry entry;
    int count = 0;
    while ((entry = nextEntry(zip, location)) != null) {
      count += analyzeAll(zip, location + "@" + entry.getName());
    }
    return count;
  }

  private ZipEntry nextEntry(ZipInputStream input, String location)
      throws IOException {
    try {
      return input.getNextEntry();
    } catch (IOException e) {
      throw analyzerError(location, e);
    }
  }

  private int analyzeGzip(final InputStream input, final String location)
      throws IOException {
    GZIPInputStream gzipInputStream;
    try {
      gzipInputStream = new GZIPInputStream(input);
    } catch (IOException e) {
      throw analyzerError(location, e);
    }
    return analyzeAll(gzipInputStream, location);
  }

  private int analyzePack200(final InputStream input, final String location)
      throws IOException {
    InputStream unpackedInput;
    try {
      unpackedInput = Pack200Streams.unpack(input);
    } catch (IOException e) {
      throw analyzerError(location, e);
    }
    return analyzeAll(unpackedInput, location);
  }

}
