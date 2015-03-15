/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;

public class NewInstrumenter {

  private final IExecutionDataAccessorGenerator accessorGenerator;
  private final Instrumenter instrumenter;

  public NewInstrumenter(IExecutionDataAccessorGenerator accessorGenerator) {
    this.accessorGenerator = accessorGenerator;
    this.instrumenter = new Instrumenter(accessorGenerator);
  }

  public byte[] instrument(final ClassLoader classLoader, final byte[] buffer, final String className) throws IOException {
    if (classLoader == null) {
      // Bootstrap ClassLoader - fallback to old instrumenter
      return instrumenter.instrument(buffer, className);
    }

    try {
      final ClassReader reader = new ClassReader(buffer);
      final long classId = CRC64.checksum(reader.b);

      final ProbeCounter counter = getProbeCounter(reader);
      if (counter.getCount() == 0) {
        return instrument(reader, new NoneProbeArrayStrategy());
      }

      new CompanionClass(className, classId, counter.getCount(), accessorGenerator).injectInto(classLoader);

      return instrument(reader, new CompanionClassProbeArrayStrategy(className));

    } catch (final RuntimeException e) {
      throw instrumentError(className, e);
    }
  }

  private IOException instrumentError(final String name, final RuntimeException cause) {
    final IOException ex = new IOException(String.format("Error while instrumenting class %s.", name));
    ex.initCause(cause);
    return ex;
  }

  private byte[] instrument(final ClassReader reader, IProbeArrayStrategy strategy) {
    final ClassWriter writer = new ClassWriter(reader, 0);
    final ClassVisitor visitor = new ClassProbesAdapter(new ClassInstrumenter(strategy, writer), true);
    reader.accept(visitor, ClassReader.EXPAND_FRAMES);
    return writer.toByteArray();
  }

  private static ProbeCounter getProbeCounter(final ClassReader reader) {
    final ProbeCounter counter = new ProbeCounter();
    reader.accept(new ClassProbesAdapter(counter, false), 0);
    return counter;
  }

}
