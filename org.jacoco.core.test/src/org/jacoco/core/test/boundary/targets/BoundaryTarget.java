/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.boundary.targets;

/**
 * Target for boundary coverage tests. Every method here contains exactly one
 * comparison, so that the boundary counter of the method is the counter of that
 * comparison.
 */
public class BoundaryTarget {

	/** Compiles to IF_ICMPLE, an ordered comparison. */
	public static boolean greaterThan(int value) {
		return value > 6;
	}

	/** Compiles to IF_ICMPNE, which has no boundary. */
	public static boolean equalTo(int value) {
		return value == 6;
	}

	/** Compiles to LCMP followed by IFLE. */
	public static boolean longGreaterThan(long value) {
		return value > 6L;
	}

	/** Compiles to DCMPL followed by IFGE. */
	public static boolean doubleLessThan(double value) {
		return value < 6d;
	}

}
