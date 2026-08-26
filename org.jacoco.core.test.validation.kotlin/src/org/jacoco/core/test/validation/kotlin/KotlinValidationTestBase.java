/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin;

import static org.junit.Assert.fail;

import org.jacoco.core.test.validation.ValidationTestBase;

import kotlin.KotlinVersion;

public abstract class KotlinValidationTestBase extends ValidationTestBase {

	protected static final KotlinVersion KOTLIN_2_1 = new KotlinVersion(2, 1);
	protected static final KotlinVersion KOTLIN_2_2 = new KotlinVersion(2, 2);

	protected KotlinValidationTestBase(final Class<?> target) {
		super(target);
	}

	/**
	 * @param kotlinVersions
	 *            Kotlin compiler versions where changes were observed, in
	 *            descending order till the version used for the first snapshot
	 */
	protected void assertSnapshot(final Class<?> targetClass,
			final String targetMethod, final String baseName,
			final KotlinVersion... kotlinVersions) throws Exception {
		for (final KotlinVersion kotlinVersion : kotlinVersions) {
			if (KotlinVersion.CURRENT.isAtLeast(kotlinVersion.getMajor(),
					kotlinVersion.getMinor())) {
				final String versionPrefix = kotlinVersion.getMajor() + "."
						+ kotlinVersion.getMinor() + "/";
				assertSnapshot(targetClass, targetMethod,
						versionPrefix + baseName);
				return;
			}
		}
		fail();
	}

}
