/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.java8;

import static org.jacoco.core.test.validation.targets.Stubs.i1;

/**
 * This test target is an interface with a class initializer.
 */
public interface InterfaceDefaultMethodsTarget {

	public static final int CONST = i1(); // $line-clinit$

	default void m1() {
		return; // $line-m1$
	}

	default void m2() {
		return; // $line-m2$
	}

	public class Impl implements InterfaceDefaultMethodsTarget {

		public Impl() {
			m1();
		}
	}

}
