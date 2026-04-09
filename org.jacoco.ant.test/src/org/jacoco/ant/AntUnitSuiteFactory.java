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
package org.jacoco.ant;

import java.io.File;

import org.apache.ant.antunit.junit3.AntUnitSuite;

final class AntUnitSuiteFactory {

	private AntUnitSuiteFactory() {
	}

	/**
	 * @return suite for given class
	 */
	static AntUnitSuite suiteFor(final Class<?> testClass) {
		final File file = new File(
				"src/org/jacoco/ant/" + testClass.getSimpleName() + ".xml");
		return new AntUnitSuite(file, testClass);
	}

	/**
	 * @return empty suite for given class
	 */
	static AntUnitSuite skip(final Class<?> testClass) {
		final File file = new File("src/org/jacoco/ant/empty.xml");
		return new AntUnitSuite(file, testClass);
	}

}
