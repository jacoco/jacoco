/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.java14;

import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java14.targets.InstanceofTarget;

/**
 * Test of code coverage in {@link InstanceofTarget}.
 */
public class InstanceofTest extends ValidationTestBase {

	public InstanceofTest() {
		super(InstanceofTarget.class);
	}

	public void assertInstanceof(final Line line) {
		if (isJDKCompiler && JAVA_VERSION.isBefore("15")) {
			// https://bugs.openjdk.java.net/browse/JDK-8237528
			assertFullyCovered(line, 1, 3);
		} else {
			assertFullyCovered(line, 0, 2);
		}
	}

}
