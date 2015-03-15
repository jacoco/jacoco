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

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

class CompanionClass {

  private static final int FIELD_ACCESS = Opcodes.ACC_SYNTHETIC
    | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_TRANSIENT
    | Opcodes.ACC_FINAL;

  private final String className;
  private final long classId;
  private final int probeCount;

  private final IExecutionDataAccessorGenerator accessorGenerator;

  private final String companionClassName;

  public CompanionClass(String className, long classId, int probeCount, IExecutionDataAccessorGenerator accessorGenerator) {
    this.className = className;
    this.classId = classId;
    this.probeCount = probeCount;
    this.accessorGenerator = accessorGenerator;

    this.companionClassName = nameFor(className);
  }

  public void injectInto(ClassLoader classLoader) {
    new ClassInjector(classLoader).inject(companionClassName.replace('/', '.'), generate());
  }

  private byte[] generate() {
    final ClassWriter cv = new ClassWriter(0);
    cv.visit(
      Opcodes.V1_5, Opcodes.ACC_SYNTHETIC, companionClassName, null,
      Type.getInternalName(Object.class),
      new String[]{}
    );

    cv.visitField(FIELD_ACCESS, InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC, null, null);

    GeneratorAdapter mv =  new GeneratorAdapter(
      cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, new String[0]),
      Opcodes.ACC_STATIC, "<clinit>", "()V");
    final int size = accessorGenerator.generateDataAccessor(classId, className, probeCount, mv);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, companionClassName, InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
    mv.returnValue();
    mv.visitMaxs(size, 0);
    mv.visitEnd();

    cv.visitEnd();

    return cv.toByteArray();
  }

  public static String nameFor(String className) {
    return className + "$jacoco";
  }

}
