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

import java.util.HashMap;

public class ProbeArrRefToExecutionDataMap {
	static private final HashMap<boolean[], ExecutionData> hashMap = new HashMap<boolean[], ExecutionData>();

	static public void set(final boolean[] array, final ExecutionData data) {
		hashMap.put(array, data);
	}

	static public ExecutionData get(final boolean[] array) {
		return hashMap.get(array);
	}
}
