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

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for <code>WorkloadAttributeMap</code>
 * 
 * @author Omer Azmon
 */
public class WorkloadAttributeMapBuilder {
	/**
	 * The attribute that is used when no other is specified, but on is required
	 */
	public static final String DEFAULT_ATTRIBUTE = "DEFAULT";
	private final Map<String, Integer> attributeMap = new HashMap<String, Integer>();

	/**
	 * Create a builder instance.
	 * 
	 * @return this builder
	 */
	public static WorkloadAttributeMapBuilder create() {
		final WorkloadAttributeMapBuilder builder = new WorkloadAttributeMapBuilder();
		return builder;
	}

	/**
	 * Create a builder instance and invoke the <code>add</code> method on the
	 * builder with the default attribute.
	 * 
	 * @param value
	 *            the value of the attribute
	 * @return this builder
	 */
	public static WorkloadAttributeMapBuilder create(
			final int value) {
		final WorkloadAttributeMapBuilder builder = new WorkloadAttributeMapBuilder();
		builder.add(DEFAULT_ATTRIBUTE, value);
		return builder;
	}

	/**
	 * Create a builder instance and invoke the <code>add</code> method on the
	 * builder.
	 * 
	 * @param attribute
	 *            the attribute to add
	 * @param value
	 *            the value of the attribute
	 * @return this builder
	 */
	public static WorkloadAttributeMapBuilder create(final String attribute,
			final int value) {
		final WorkloadAttributeMapBuilder builder = new WorkloadAttributeMapBuilder();
		builder.add(attribute, value);
		return builder;
	}

	/**
	 * Add an attribute
	 * 
	 * @param attribute
	 *            the attribute to add
	 * @param value
	 *            the value of the attribute
	 * @return this builder
	 */
	public WorkloadAttributeMapBuilder add(final String attribute,
			final int value) {
		attributeMap.put(attribute, value);
		return this;
	}

	/**
	 * Build the workload attribute map with the attributes added so far.
	 * 
	 * @return the workload attribute map with the attributes added so far.
	 */
	public WorkloadAttributeMap build() {
		return new WorkloadAttributeMap(attributeMap);
	}
}