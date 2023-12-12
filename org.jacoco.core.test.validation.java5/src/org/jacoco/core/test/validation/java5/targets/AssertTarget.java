/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.t;

/**
 * This target exercises assert statement.
 */
public class AssertTarget { // assertFullyCovered()

	private AssertTarget() {
	}

	public static void main(String[] args) {
		assert t() : "msg"; // assertPartlyCovered(1, 1)
	}

}
