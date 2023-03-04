/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.t;

import org.jacoco.core.test.validation.targets.Stubs.SuperClass;

/**
 * This test target has a constructor containing control structures before the
 * superclass constructor is called.
 */
public class ControlStructureBeforeSuperConstructorTarget extends SuperClass {

	public ControlStructureBeforeSuperConstructorTarget() {
		super(t() || f()); // assertPartlyCovered(3, 1)
	}

	public static void main(String[] args) {
		new ControlStructureBeforeSuperConstructorTarget();
	}

}
