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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class CompanionClassProbeArrayStrategy implements IProbeArrayStrategy {
  private final String className;

  public CompanionClassProbeArrayStrategy(String className) {
    this.className = className;
  }

  public int storeInstance(MethodVisitor mv, int variable) {
    mv.visitFieldInsn(Opcodes.GETSTATIC, CompanionClass.nameFor(className), InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
    mv.visitVarInsn(Opcodes.ASTORE, variable);
    return 1;
  }

  public void addMembers(ClassVisitor cv, int probeCount) {
    // nothing to do
  }

}
