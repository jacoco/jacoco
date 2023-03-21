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
package org.jacoco.core.test.validation;

/**
 * Parsed value of "java.version" system property.
 */
public final class JavaVersion {

	private final int feature;

	private final int update;

	/**
	 * @param javaVersionPropertyValue
	 *            value of "java.version" property
	 * @see System#getProperties() description of properties
	 */
	JavaVersion(final String javaVersionPropertyValue) {
		final String[] s = javaVersionPropertyValue.split("[._-]");
		if ("1".equals(s[0])) {
			this.feature = Integer.parseInt(s[1]);
			this.update = s.length > 3 ? Integer.parseInt(s[3]) : 0;
		} else {
			this.feature = Integer.parseInt(s[0]);
			this.update = s.length > 2 ? Integer.parseInt(s[2]) : 0;
		}
	}

	/**
	 * @return value of feature-release counter, for example: 8 for version
	 *         "1.8.0_152" and 9 for version "9.0.1"
	 */
	int feature() {
		return feature;
	}

	/**
	 * @return value of update-release counter, for example: 152 for version
	 *         "1.8.0_152" and 1 for version "9.0.1"
	 */
	int update() {
		return update;
	}

	/**
	 * @param version
	 *            version to compare with
	 * @return <code>true</code> if this version is less than given
	 */
	public boolean isBefore(final String version) {
		final JavaVersion other = new JavaVersion(version);
		return this.feature < other.feature || (this.feature == other.feature
				&& this.update < other.update);
	}

	/**
	 * @return version of the current JVM
	 */
	public static JavaVersion current() {
		return new JavaVersion(System.getProperty("java.version"));
	}

}
