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

import static java.lang.String.format;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory data store for execution data. The data can be added through its
 * {@link IExecutionDataVisitor} interface. If execution data is provided
 * multiple times for the same class the data is merged, i.e. a block is marked
 * as executed if it is reported as executed at least once. This allows to merge
 * coverage date from multiple runs. A instance of this class is not thread
 * safe.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecutionDataStore implements IExecutionDataVisitor {

	private final Map<Long, boolean[]> data = new HashMap<Long, boolean[]>();

	private final Map<Long, String> names = new HashMap<Long, String>();

	/**
	 * Adds the given block data structure into the store. If there is already a
	 * data structure for this class ID, this structure is merged with the given
	 * one. In this case a {@link IllegalStateException} is thrown, if both
	 * executions data structure do have different sizes or the name is
	 * different.
	 * 
	 * @param classid
	 *            unique class identifier
	 * @param name
	 *            VM name of the class
	 * @param data
	 *            execution data
	 */
	public void put(final Long classid, final String name, boolean[] data) {
		final boolean[] current = this.data.get(classid);
		if (current != null) {
			checkName(classid, name);
			merge(current, data);
			data = current;
		}
		this.names.put(classid, name);
		this.data.put(classid, data);
	}

	/**
	 * Adds the given block data structure into the store. If there is already a
	 * data structure for this class ID, this structure is merged with the given
	 * one. In this case a {@link IllegalStateException} is thrown, if both
	 * executions data structure do have different sizes or the name is
	 * different.
	 * 
	 * @param classid
	 *            unique class identifier
	 * @param name
	 *            VM name of the class
	 * @param data
	 *            execution data
	 */
	public void put(final long classid, final String name, final boolean[] data) {
		put(Long.valueOf(classid), name, data);
	}

	private void checkName(final Long classid, final String name) {
		final String oldName = names.get(classid);
		if (!name.equals(oldName)) {
			throw new IllegalArgumentException(format(
					"Duplicate id %x for classes %s and %s.", classid, oldName,
					name));
		}
	}

	private static void merge(final boolean[] target, final boolean[] data) {
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
	public boolean[] getData(final long classid) {
		return getData(Long.valueOf(classid));
	}

	/**
	 * Returns the coverage data for the class with the given identifier if
	 * available.
	 * 
	 * @param classid
	 *            class identifier
	 * @return coverage data or <code>null</code>
	 */
	public boolean[] getData(final Long classid) {
		return data.get(classid);
	}

	/**
	 * Returns the vm name of the class with the given id.
	 * 
	 * @param classid
	 *            class identifier
	 * @return vm name or <code>null</code>
	 */
	public String getName(final long classid) {
		return getName(Long.valueOf(classid));
	}

	/**
	 * Returns the vm name of the class with the given id.
	 * 
	 * @param classid
	 *            class identifier
	 * @return vm name or <code>null</code>
	 */
	public String getName(final Long classid) {
		return names.get(classid);
	}

	/**
	 * Resets all execution data structures, i.e. marks them as not executed.
	 * The data structures itself are not deleted.
	 */
	public void reset() {
		for (final boolean[] d : this.data.values()) {
			Arrays.fill(d, false);
		}
	}

	/**
	 * Writes the content of the store to the given visitor interface.
	 * 
	 * @param visitor
	 *            interface to write content to
	 */
	public void accept(final IExecutionDataVisitor visitor) {
		for (final Map.Entry<Long, boolean[]> entry : data.entrySet()) {
			final Long key = entry.getKey();
			final long id = key.longValue();
			visitor.visitClassExecution(id, names.get(key), entry.getValue());
		}
	}

	// === IExecutionDataVisitor ===

	public void visitClassExecution(final long classid, final String name,
			final boolean[] data) {
		put(classid, name, data);
	}

}
