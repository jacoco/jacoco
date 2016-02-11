/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.analysis;

import java.util.SortedMap;
import java.util.TreeMap;

import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMap;

/**
 * An ordered list of points on the X-Axis in ascending X-value order.
 * 
 * @author Omer Azmon
 */
public class XAxisValues {
	private final SortedMap<Integer, WorkloadAttributeMap> xAxisMap;

	/**
	 * Construct an ordered list of points on the X-Axis in ascending X-value
	 * order.
	 * 
	 * @param store
	 *            each workload in the store contributes on X-value
	 * @param attributeName
	 *            the attribute name used to query each workload for its
	 *            X-value. Different analysis attempts may use different X-value
	 *            attributes.
	 * @throws IllegalArgumentException
	 *             if the attributeName is not a required attribute in the
	 *             store.
	 */
	public XAxisValues(final EmpiricalBigOWorkloadStore store,
			final String attributeName) {
		if (!store.getRequiredAttributes().contains(attributeName)) {
			throw new IllegalArgumentException("Attribute '" + attributeName
					+ "' is not found in this map.");
		}

		// Build the map
		xAxisMap = new TreeMap<Integer, WorkloadAttributeMap>();
		for (WorkloadAttributeMap attribute : store.keySet()) {
			final Integer value = attribute.get(attributeName);
			xAxisMap.put(value, attribute);
		}
	}

	/**
	 * Returns a {@code WorkloadAttributeMap[]} array of keys into the
	 * {@code EmpiricalBigOWorkloadStore} in the order of X-values associated
	 * with the attribute name provided in construction.
	 * 
	 * @return an array of workload store keys in the order of X-values
	 *         associated with the attribute name provided in construction.
	 */
	public WorkloadAttributeMap[] getXKeys() {
		return xAxisMap.values().toArray(
				new WorkloadAttributeMap[xAxisMap.size()]);
	}

	/**
	 * Returns the number of X-values
	 * 
	 * @return the number of X-values
	 */
	public int size() {
		return xAxisMap.size();
	}

	/**
	 * Returns the X-values in ascending order.
	 * 
	 * @return the X-values in ascending order.
	 */
	public int[] getXValues() {
		final int[] xValues = new int[xAxisMap.size()];
		int idx = 0;
		for (final Integer key : xAxisMap.keySet()) {
			xValues[idx++] = key;
		}
		return xValues;
	}

	public String toString() {
		return xAxisMap.toString();
	}
}