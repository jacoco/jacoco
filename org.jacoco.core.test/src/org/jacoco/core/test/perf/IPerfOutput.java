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
package org.jacoco.core.test.perf;

/**
 * Interface to report performance figures to.
 */
public interface IPerfOutput {

	/** Indicator for no reference time given */
	long NO_REFERENCE = Long.MIN_VALUE;

	/**
	 * Reports the result of a time measurement with a optional reference time
	 * for comparison.
	 *
	 * @param description
	 *            textual description of the test case
	 * @param duration
	 *            duration in nano seconds
	 * @param reference
	 *            optional reference time in nano seconds
	 */
	void writeTimeResult(String description, long duration, long reference);

	/**
	 * Reports the result of a byte size measurement with a optional reference
	 * size for comparison.
	 *
	 * @param description
	 *            textual description of the test case
	 * @param size
	 *            size in bytes
	 * @param reference
	 *            optional reference size in bytes
	 */
	void writeByteResult(String description, long size, long reference);

}
