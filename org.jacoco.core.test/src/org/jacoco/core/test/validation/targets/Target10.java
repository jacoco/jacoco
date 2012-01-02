/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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
		super(t() ? t() : f()); // $line-super$

		// The following construct causes an VerifyError with the message
		// "Uninitialized object exists on backward branch" on Oracle 1.6 VMs.
		// Actually JaCoCo inserts a probe into the conditional jump here which
		// results in a backward jump.
		// It is not clear whether the VM implementations are in line with the
		// latest VM specification update here.

		// super(t() || f());
	}

	public static void main(String[] args) {
		new Target10();
	}

}
