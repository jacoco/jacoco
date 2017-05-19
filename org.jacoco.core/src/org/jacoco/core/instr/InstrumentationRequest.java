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


import org.jacoco.core.internal.instr.SignatureRemover;
import org.objectweb.asm.ClassReader;

/**
 * An InstrumentationRequest is a request to instrument a specific ClassReader
 * It may also request that the class be upgraded to a different JVM version afterward
 */
public class InstrumentationRequest {

	private ClassReader classReader;
	private boolean upgradeToJava9 = false;


  public InstrumentationRequest(ClassReader classReader) {
		this.classReader = classReader;
	}

  /**
   * Returns the ClassReader to be instrumented
   * @return the class reader to be instrumented
   */
	public ClassReader getClassReader() {
	  return classReader;
  }

  /**
   * Specify whether to upgrade to Java 9 after instrumenting
   * @param upgradeToJava9 whether to upgrade to Java 9 after instrumenting
   * @return this object (for chaining)
   */
	public InstrumentationRequest setUpgradeToJava9(boolean upgradeToJava9) {
		this.upgradeToJava9 = upgradeToJava9;
		return this;
	}

  /**
   * Tells whether to upgrade to Java 9
   * @return whether to upgrade to Java 9
   */
	public boolean getUpgradeToJava9() {
		return upgradeToJava9;
	}
}
