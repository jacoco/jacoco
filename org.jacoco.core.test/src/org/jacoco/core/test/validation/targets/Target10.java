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
package org.jacoco.core.test.validation.targets;

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.t;

import org.jacoco.core.test.validation.targets.Stubs.SuperClass;

/**
 * This test target has a constructor containing control structures before the
 * superclass constructor is called.
 */
public class Target10 extends SuperClass {

	public Target10() {
		super(t() || f()); // $line-super$
	}

	public static void main(String[] args) {
		new Target10();
	}

}
