/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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

	private final Map<Long, Entry> entries = new HashMap<Long, Entry>();

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
	public void put(final Long classid, final String name, final boolean[] data) {
		final Entry entry = entries.get(classid);
		if (entry == null) {
			entries.put(classid, new Entry(name, data));
		} else {
			entry.merge(classid, name, data);
		}
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

	/**
	 * Returns the coverage data for the class with the given identifier if
	 * available.
	 * 
	 * @param classid
	 *            class identifier
	 * @return coverage data or <code>null</code>
	 */
	public boolean[] getData(final Long classid) {
		final Entry entry = entries.get(classid);
		return entry == null ? null : entry.data;
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
	 * Returns the coverage date for the class with the given identifier. If
	 * there is not data available under the given id a new entry is created.
	 * 
	 * @param classid
	 *            class identifier
	 * @param name
	 *            VM name of the class
	 * @param probecount
	 *            probe array length
	 * @return execution data
	 */
	public boolean[] getData(final Long classid, final String name,
			final int probecount) {
		Entry entry = entries.get(classid);
		if (entry == null) {
			entry = new Entry(name, new boolean[probecount]);
			entries.put(classid, entry);
		} else {
			entry.checkCompatibility(classid, name, probecount);
		}
		return entry.data;
	}

	/**
	 * Returns the vm name of the class with the given id.
	 * 
	 * @param classid
	 *            class identifier
	 * @return vm name or <code>null</code>
	 */
	public String getName(final Long classid) {
		final Entry entry = entries.get(classid);
		return entry == null ? null : entry.name;
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
	 * Resets all execution data structures, i.e. marks them as not executed.
	 * The data structures itself are not deleted.
	 */
	public void reset() {
		for (final Entry executionData : this.entries.values()) {
			Arrays.fill(executionData.data, false);
		}
	}

	/**
	 * Writes the content of the store to the given visitor interface.
	 * 
	 * @param visitor
	 *            interface to write content to
	 */
	public void accept(final IExecutionDataVisitor visitor) {
		for (final Map.Entry<Long, Entry> i : entries.entrySet()) {
			final long id = i.getKey().longValue();
			final Entry entry = i.getValue();
			visitor.visitClassExecution(id, entry.name, entry.data);
		}
	}

	// === IExecutionDataVisitor ===

	public void visitClassExecution(final long classid, final String name,
			final boolean[] data) {
		put(classid, name, data);
	}

	private static class Entry {

		final String name;
		final boolean[] data;

		Entry(final String name, final boolean[] data) {
			this.name = name;
			this.data = data;
		}

		void checkCompatibility(final Long classid, final String otherName,
				final int otherLength) {
			if (!otherName.equals(name)) {
				throw new IllegalStateException(format(
						"Duplicate id %x for classes %s and %s.", classid,
						name, otherName));
			}
			if (data.length != otherLength) {
				throw new IllegalStateException(format(
						"Incompatible execution data for class %s (id %s).",
						name, classid));
			}
		}

		void merge(final Long classid, final String newName,
				final boolean[] newData) {
			checkCompatibility(classid, newName, newData.length);
			for (int i = 0; i < data.length; i++) {
				if (!data[i]) {
					data[i] = newData[i];
				}
			}
		}
	}

}
