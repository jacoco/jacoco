/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.core;

import static org.jacoco.ebigo.internal.util.ValidationUtils.validateNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.ebigo.analysis.XAxisValues;

/**
 * A data store of workloads indexed by their attributes.
 * 
 * @author Omer Azmon
 */
public class EmpiricalBigOWorkloadStore {
	private final ArrayList<String> requiredAttributes;
	private final HashMap<WorkloadAttributeMap, EmpiricalBigOWorkload> actualMap;

	/**
	 * Construct a new workload store with the default attribute
	 */
	public EmpiricalBigOWorkloadStore() {
		this(WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE);
	}
	
	/**
	 * Construct a new workload store
	 * 
	 * @param requiredAttribute
	 *            an attribute that must be defined by any workload added to
	 *            this store.
	 * @param moreRequiredAttributes
	 *            additional attributes that must be defined by any workload
	 *            added to this store.
	 */
	public EmpiricalBigOWorkloadStore(final String requiredAttribute,
			final String... moreRequiredAttributes) {
		if (requiredAttribute == null) {
			throw new IllegalArgumentException(
					"Must have at least one attribute");
		}
		final int moreLength = moreRequiredAttributes == null ? 0
				: moreRequiredAttributes.length;
		this.requiredAttributes = new ArrayList<String>(1 + moreLength);
		this.requiredAttributes.add(requiredAttribute);
		if (moreRequiredAttributes != null) {
			this.requiredAttributes.addAll(Arrays
					.asList(moreRequiredAttributes));
		}
		actualMap = new HashMap<WorkloadAttributeMap, EmpiricalBigOWorkload>();
	}

	public List<String> getRequiredAttributes() {
		return Collections.unmodifiableList(requiredAttributes);
	}

	public String getDefaultAttribute() {
		return requiredAttributes.get(0);
	}

	private void validateRequiredAttriubtes(final WorkloadAttributeMap map) {
		validateNotNull("map", map);
		for (final String attribute : requiredAttributes) {
			if (!map.containsKey(attribute)) {
				throw new IllegalArgumentException(
						"Missing required attribute: " + attribute);
			}
		}
	}

	private void validateNotDup(final WorkloadAttributeMap map) {
		validateNotNull("map", map);
		for (final Entry<String, Integer> attributeEntry : map.entrySet()) {
			for (final WorkloadAttributeMap attribute : actualMap.keySet()) {
				final Integer value = attribute.get(attributeEntry.getKey());
				if (value != null
						&& value.intValue() == attributeEntry.getValue()
								.intValue()) {
					throw new IllegalArgumentException(
							"Duplicate attribute/value combination in store; attribute="
									+ attributeEntry.getKey() + ", value="
									+ attributeEntry.getValue());

				}
			}
		}

	}

	/**
	 * Add a workload to this store.
	 * 
	 * @param workload
	 *            the workload to add to the store.
	 * @return the previous value associated with <tt>attributes</tt>, or
	 *         <tt>null</tt> if there was no mapping for <tt>attributes</tt>.
	 */
	public EmpiricalBigOWorkload put(final EmpiricalBigOWorkload workload) {
		final WorkloadAttributeMap attributes = workload.getattributeMap();
		validateRequiredAttriubtes(attributes);
		validateNotDup(attributes);
		return actualMap.put(attributes, workload);
	}

	/**
	 * Get a workload by its key.
	 * 
	 * @param key
	 *            the attributes associated with this workload and their values.
	 *            This object is the 'key' of the workload in the store.
	 * @return the workload associated with the key, or {@code null} if not
	 *         found.
	 */
	public EmpiricalBigOWorkload get(final WorkloadAttributeMap key) {
		return actualMap.get(key);
	}

	/**
	 * Returns the number of workloads currently in the store.
	 * 
	 * @return the number of workloads in the store.
	 */
	public int size() {
		return actualMap.size();
	}

	/**
	 * Returns an immutable {@link Set} view of the keys contained in this
	 * store. The set is backed by the store. If the map is modified while an
	 * iteration over the set is in progress, the results of the iteration are
	 * undefined.
	 *
	 * @return a set view of the keys contained in this store. Each key is an
	 *         X-axis attributes and values map.
	 */
	public Set<WorkloadAttributeMap> keySet() {
		return Collections.unmodifiableSet(actualMap.keySet());
	}

	/**
	 * Generates and returns an immutable sorted index from X-axis values to
	 * workloads in the store. The order is ascending X-axis value.
	 * <p>
	 * Any workload added to the store after this index has been generated, is
	 * NOT included in the index.
	 * 
	 * @param attributeName
	 *            the X-Axis attribute for which the index is generated.
	 * @return the index from X-Values to
	 */
	public XAxisValues getXAxisValues(final String attributeName) {
		return new XAxisValues(this, attributeName);
	}

	/**
	 * Returns an merged execution data store from all the workload data
	 * currently in this store.
	 * 
	 * @return an merged execution data store.
	 */
	public ExecutionDataStore getMergedExecutionDataStore() {
		final ExecutionDataStore store = new ExecutionDataStore();
		for (final WorkloadAttributeMap key : keySet()) {
			final EmpiricalBigOWorkload workload = get(key);
			for (final ExecutionData data : workload.getExecutionDataStore()
					.getContents()) {
				// As we don't want to change the data in this store
				// While producing the summary
				store.put(data.deepCopy());
			}
		}
		return store;
	}

	/**
	 * Returns an merged session info store from all the workload data currently
	 * in this store.
	 * 
	 * @return an merged session info store.
	 */
	public SessionInfoStore getMergedSessionInfoStore() {
		final SessionInfoStore store = new SessionInfoStore();
		for (final WorkloadAttributeMap key : keySet()) {
			final EmpiricalBigOWorkload workload = get(key);
			for (final SessionInfo info : workload.getSessionInfo().getInfos()) {
				store.visitSessionInfo(info);
			}
		}
		return store;
	}

}