/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.data;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory data store for execution data. The data can be added through its
 * {@link IExecutionDataOutput} interface. If execution data is provided
 * multiple times for the same class the data is merged, i.e. a block is marked
 * as executed if it is reported as executed at least once. This allows to merge
 * coverage date from multiple runs. This class is not thread safe.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecutionDataStore implements IExecutionDataOutput {

	private final Map<Long, boolean[][]> data = new HashMap<Long, boolean[][]>();

	public void classExecution(long classid, boolean[][] blockdata) {
		boolean[][] current = data.get(classid);
		if (current != null) {
			merge(current, blockdata);
			blockdata = current;
		}
		data.put(Long.valueOf(classid), blockdata);

	}

	private static void merge(boolean[][] target, boolean[][] data) {
		if (target.length != data.length) {
			throw new IllegalStateException("Incompatible execution data.");
		}
		for (int i = 0; i < target.length; i++) {
			merge(target[i], data[i]);
		}
	}

	private static void merge(boolean[] target, boolean[] data) {
		if (target.length != data.length) {
			throw new IllegalStateException("Incompatible execution data.");
		}
		for (int i = 0; i < target.length; i++) {
			if (!target[i]) {
				target[i] = data[i];
			}
		}
	}

	/**
	 * Returns the coverage data for the class with the given identifier if
	 * available.
	 * 
	 * @param classid
	 *            class identifier
	 * @return coverage data or <code>null</code>
	 */
	public boolean[][] getBlockdata(long classid) {
		return data.get(classid);
	}

	/**
	 * Writes the content of the store to the given output interface.
	 * 
	 * @param output
	 *            interface to write content to
	 */
	public void writeTo(IExecutionDataOutput output) {
		for (final Map.Entry<Long, boolean[][]> entry : data.entrySet()) {
			output.classExecution(entry.getKey(), entry.getValue());
		}
	}

}
