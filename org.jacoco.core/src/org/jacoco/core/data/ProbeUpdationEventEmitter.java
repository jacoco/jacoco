/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Vineet Bakshi - API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.data;

interface ProbeUpdateListener {
	void listener(long classId, int probeId, String className,
			boolean[] probes);
}

public class ProbeUpdationEventEmitter {
	static ProbeUpdateListener probeUpdateListener = null;

	public static void updateProbe(boolean[] probes, int probeId) {
		if (probeUpdateListener != null) {
			ExecutionData executionData = ProbeArrRefToExecutionDataMap
					.get(probes);
			long classId = executionData.getId();
			String className = executionData.getName();

			probeUpdateListener.listener(classId, probeId, className, probes);
		}
	};

	public static void addListener(ProbeUpdateListener probeUpdateListener) {
		ProbeUpdationEventEmitter.probeUpdateListener = probeUpdateListener;
		System.out.println("Successfully added probe updation listener!");
	}

	public static void noopFn() {}

	// A private constructor to prevent instantiation
	private ProbeUpdationEventEmitter() {
	}
}
