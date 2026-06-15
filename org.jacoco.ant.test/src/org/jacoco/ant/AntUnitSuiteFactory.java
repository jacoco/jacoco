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
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.ant.antunit.junit3.AntUnitSuite;

final class AntUnitSuiteFactory {

	private AntUnitSuiteFactory() {
	}

	/**
	 * @return suite for given {@code testClass}
	 */
	static AntUnitSuite suiteFor(final Class<?> testClass) {
		return new AntUnitSuite(
				getResource(testClass, testClass.getSimpleName() + ".xml"),
				testClass);
	}

	/**
	 * @return empty suite for given {@code testClass}
	 */
	static AntUnitSuite skip(final Class<?> testClass) {
		return new AntUnitSuite(getResource(testClass, "empty.xml"),
				testClass);
	}

	/** @return file for classpath resource next to {@code testClass} */
	private static File getResource(final Class<?> testClass,
			final String resource) {
		final URL url = testClass.getResource(resource);
		if (url == null) {
			throw new IllegalArgumentException(
					"Resource not found: " + resource);
		}
		try {
			return new File(url.toURI());
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(
					"Invalid resource URL: " + url, e);
		}
	}

}
