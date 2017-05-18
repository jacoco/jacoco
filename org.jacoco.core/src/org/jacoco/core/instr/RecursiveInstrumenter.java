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
import org.jacoco.core.internal.Java9Support;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.internal.instr.ProbeArrayStrategyFactory;
import org.jacoco.core.internal.instr.SignatureRemover;
import org.jacoco.core.matcher.ClassnameMatcher;
import org.jacoco.core.matcher.Matcher;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * Several APIs to instrument Java class definitions for coverage tracing.
 */
public class RecursiveInstrumenter {

  private Matcher<ClassReader> instrumentationPredicate;
  private Instrumenter instrumenter;
  private final SignatureRemover signatureRemover = new SignatureRemover();


  /**
   * Creates a new instance based on the given runtime.
   *
   * @param runtime
   *            runtime used by the instrumented classes
   */
  public RecursiveInstrumenter(final IExecutionDataAccessorGenerator runtime) {
    instrumenter = new Instrumenter(runtime);
    this.instrumentationPredicate = new ClassnameMatcher();
  }

  /**
   * Creates a new instance based on the given runtime.
   * This instrumenter will skip instrumentation of any classes excluded by instrumentationPredicate
   *
   * @param runtime
   *            runtime used by the instrumented classes
   * @param instrumentationPredicate
   *            matcher to determine which classes to instrument
   */
  public RecursiveInstrumenter(final IExecutionDataAccessorGenerator runtime, Matcher<ClassReader> instrumentationPredicate) {
    instrumenter = new Instrumenter(runtime);
    this.instrumentationPredicate = instrumentationPredicate;
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
   * Creates an instrumented version of the given class file.
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
  private int instrumentIfNotExcluded(final InputStream input, final OutputStream output,
      final String name) throws IOException {
    InstrumentationRequest request = instrumenter.asClassReader(input, name);
    boolean included;
    try {
      included = instrumentationPredicate.matches(request.getClassReader());
    } catch (ArrayIndexOutOfBoundsException e) {
      throw instrumenter.instrumentError(name, e);
    }
    if (included) {
      output.write(instrumenter.instrument(request));
      return 1;
    } else {
      output.write(request.getClassReader().b);
      //copy(request.getClassReader().b, output, name);
      return 0;
    }
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
    final ContentTypeDetector detector;
    try {
      detector = new ContentTypeDetector(input);
    } catch (IOException e) {
      throw instrumenter.instrumentError(name, e);
    }
    switch (detector.getType()) {
      case ContentTypeDetector.CLASSFILE:
        return instrumentIfNotExcluded(detector.getInputStream(), output, name);
      case ContentTypeDetector.ZIPFILE:
        return instrumentZip(detector.getInputStream(), output, name);
      case ContentTypeDetector.GZFILE:
        return instrumentGzip(detector.getInputStream(), output, name);
      case ContentTypeDetector.PACK200FILE:
        return instrumentPack200(detector.getInputStream(), output, name);
      default:
        copy(detector.getInputStream(), output, name);
        return 0;
    }
  }

  private int instrumentZip(final InputStream input,
      final OutputStream output, final String name) throws IOException {
    final ZipInputStream zipin = new ZipInputStream(input);
    final ZipOutputStream zipout = new ZipOutputStream(output);
    ZipEntry entry;
    int count = 0;
    while ((entry = nextEntry(zipin, name)) != null) {
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

  private ZipEntry nextEntry(ZipInputStream input, String location)
      throws IOException {
    try {
      return input.getNextEntry();
    } catch (IOException e) {
      throw instrumenter.instrumentError(location, e);
    }
  }

  private int instrumentGzip(final InputStream input,
      final OutputStream output, final String name) throws IOException {
    final GZIPInputStream gzipInputStream;
    try {
      gzipInputStream = new GZIPInputStream(input);
    } catch (IOException e) {
      throw instrumenter.instrumentError(name, e);
    }
    final GZIPOutputStream gzout = new GZIPOutputStream(output);
    final int count = instrumentAll(gzipInputStream, gzout, name);
    gzout.finish();
    return count;
  }

  private int instrumentPack200(final InputStream input,
      final OutputStream output, final String name) throws IOException {
    final InputStream unpackedInput;
    try {
      unpackedInput = Pack200Streams.unpack(input);
    } catch (IOException e) {
      throw instrumenter.instrumentError(name, e);
    }
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final int count = instrumentAll(unpackedInput, buffer, name);
    Pack200Streams.pack(buffer.toByteArray(), output);
    return count;
  }

  private void copy(final InputStream input, final OutputStream output,
      final String name) throws IOException {
    final byte[] buffer = new byte[1024];
    int len;
    while ((len = read(input, buffer, name)) != -1) {
      output.write(buffer, 0, len);
    }
  }

  private int read(final InputStream input, final byte[] buffer,
      final String name) throws IOException {
    try {
      return input.read(buffer);
    } catch (IOException e) {
      throw instrumenter.instrumentError(name, e);
    }
  }

}
