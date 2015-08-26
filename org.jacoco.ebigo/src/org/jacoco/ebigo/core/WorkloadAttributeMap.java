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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A map of the attributes (X-Values) associated with a workload.
 * 
 * @author Omer Azmon
 */
public class WorkloadAttributeMap extends AbstractMap<String, Integer> {
	private final Map<String, Integer> actualMap;

	/**
	 * Constructor
	 */
	public WorkloadAttributeMap() {
		this(null);
	}

	/**
	 * Constructor for the AttributeMapBuilder
	 * 
	 * @param actualMap
	 */
	WorkloadAttributeMap(final Map<String, Integer> attributeMap) {
		this.actualMap = Collections
				.unmodifiableMap(attributeMap != null ? attributeMap
						: new HashMap<String, Integer>());
	}

	@Override
	public Set<java.util.Map.Entry<String, Integer>> entrySet() {
		return actualMap.entrySet();
	}
}
