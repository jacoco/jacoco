/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores covered instruction signatures for methods.
 */
public class InstructionCoverageStore {

	private final Map<String, Set<String>> covered = new HashMap<String, Set<String>>();

	private static String methodKey(final String className,
			final String methodName, final String methodDesc) {
		return className + "#" + methodName + methodDesc;
	}

	public void recordCoveredInstruction(final String className,
			final String methodName, final String methodDesc,
			final String instructionSign) {
		if (instructionSign == null) {
			return;
		}
		final String key = methodKey(className, methodName, methodDesc);
		Set<String> set = covered.get(key);
		if (set == null) {
			set = new HashSet<String>();
			covered.put(key, set);
		}
		set.add(instructionSign);
	}

	public boolean isInstructionCovered(final String className,
			final String methodName, final String methodDesc,
			final String instructionSign) {
		if (instructionSign == null) {
			return false;
		}
		final String key = methodKey(className, methodName, methodDesc);
		final Set<String> set = covered.get(key);
		return set != null && set.contains(instructionSign);
	}

	public int size() {
		int total = 0;
		for (final Set<String> set : covered.values()) {
			total += set.size();
		}
		return total;
	}
}
