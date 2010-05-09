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

/**
 * Execution data for a single Java class. While instances are immutable care
 * has to be taken about the probe data array of type <code>boolean[]</code>
 * which can be modified.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public final class ExecutionData {

	private final long id;

	private final String name;

	private final boolean[] data;

	/**
	 * Creates a new {@link ExecutionData} object with the given probe data.
	 * 
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name
	 * @param data
	 *            probe data
	 */
	public ExecutionData(final long id, final String name, final boolean[] data) {
		this.id = id;
		this.name = name;
		this.data = data;
	}

	/**
	 * Creates a new {@link ExecutionData} object with the given probe data
	 * length. All probes are set to <code>false</code>.
	 * 
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name
	 * @param dataLength
	 *            probe data length
	 */
	public ExecutionData(final long id, final String name, final int dataLength) {
		this.id = id;
		this.name = name;
		this.data = new boolean[dataLength];
	}

	/**
	 * Return the unique identifier for this class. The identifier is the CRC64
	 * checksum of the raw class file definition.
	 * 
	 * @return class identifier
	 */
	public long getId() {
		return id;
	}

	/**
	 * The VM name of the class.
	 * 
	 * @return VM name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the execution data probes. A value of <code>true</code> indicates
	 * that the corresponding probe was executed.
	 * 
	 * @return execution data
	 */
	public boolean[] getData() {
		return data;
	}

	/**
	 * Sets all probe data entries to <code>false</code>.
	 */
	public void reset() {
		Arrays.fill(data, false);
	}

	/**
	 * Merges the given execution data into the probe data of this object. I.e.
	 * a probe entry in this object is marked as executed (<code>true</code>) if
	 * this probe or the corresponding other probe was executed. The probe array
	 * of the other object is not modified.
	 * 
	 * @param other
	 */
	public void merge(final ExecutionData other) {
		assertCompatibility(other.getId(), other.getName(),
				other.getData().length);
		final boolean[] otherData = other.getData();
		for (int i = 0; i < data.length; i++) {
			if (otherData[i]) {
				data[i] = true;
			}
		}
	}

	/**
	 * Asserts that this execution data object is compatible with the given
	 * parameters. The purpose of this check is to detect a very unlikely class
	 * id collision.
	 * 
	 * @param id
	 *            other class id, must be the same
	 * @param name
	 *            other name, must be equal to this name
	 * @param dataLength
	 *            probe data length, must be the same as for this data
	 * @throws IllegalStateException
	 *             if the given parameters do not match this instance
	 */
	public void assertCompatibility(final long id, final String name,
			final int dataLength) throws IllegalStateException {
		if (this.id != id) {
			throw new IllegalStateException(format(
					"Different ids (%016x and %016x).", Long.valueOf(this.id),
					Long.valueOf(id)));
		}
		if (!this.name.equals(name)) {
			throw new IllegalStateException(format(
					"Different class names %s and %s for id %016x.", this.name,
					name, Long.valueOf(id)));
		}
		if (this.data.length != dataLength) {
			throw new IllegalStateException(format(
					"Incompatible execution data for class %s with id %016x.",
					name, Long.valueOf(id)));
		}
	}

}
