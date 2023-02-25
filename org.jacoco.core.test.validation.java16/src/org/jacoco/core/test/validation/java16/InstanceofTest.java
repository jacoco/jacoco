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
package org.jacoco.core.test.validation.java16;

import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java16.targets.InstanceofTarget;

/**
 * Test of code coverage in {@link InstanceofTarget}.
 */
public class InstanceofTest extends ValidationTestBase {

	public InstanceofTest() {
		super(InstanceofTarget.class);
	}

	public void assertInstanceof(Line line) {
		if (isJDKCompiler) {
			assertFullyCovered(line, 0, 2);
		} else {
			// Upgrade to ECJ version with
			// https://github.com/eclipse-jdt/eclipse.jdt.core/commit/3b4c932227240d090904e141a485ba9181a79b67
			// will lead to the absence of missed branches
			assertFullyCovered(line, 1, 3);
		}
	}

}
