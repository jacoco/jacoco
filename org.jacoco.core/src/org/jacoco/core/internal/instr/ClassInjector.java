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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

class ClassInjector {

  private static final Method DEFINE_CLASS_METHOD;

  private final ClassLoader classLoader;

  static {
    try {
      DEFINE_CLASS_METHOD = ClassLoader.class.getDeclaredMethod("defineClass",
        String.class,
        byte[].class,
        int.class,
        int.class,
        ProtectionDomain.class);
      DEFINE_CLASS_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public ClassInjector(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public Class<?> inject(String name, byte[] binaryRepresentation) {
    try {
      synchronized (classLoader) {
        return (Class<?>) DEFINE_CLASS_METHOD.invoke(
          classLoader,
          name,
          binaryRepresentation,
          0,
          binaryRepresentation.length,
          null
        );
      }
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } catch (InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

}
