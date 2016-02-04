/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

import static java.lang.String.format;

import org.jacoco.core.internal.instr.IProbeArray;
import org.jacoco.core.internal.instr.ProbeArrayService;

/**
 * Execution data for a single Java class. While instances are immutable care
 * has to be taken about the probe data array of type
 * <code>{@literal IProbeArray<?>}</code> which can be modified.
 */
public final class ExecutionData {

	private final long id;

	private final String name;

	private final IProbeArray<?> probes;

	/**
	 * Creates a new {@link ExecutionData} object with the given probe data.
	 * 
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name
	 * @param dataObject
	 *            probe data must be an IProbeArray or the internal object of
	 *            one of the supported probe types.
	 */
	public ExecutionData(final long id, final String name,
			final Object dataObject) {
		this.id = id;
		this.name = name;
		if (dataObject instanceof IProbeArray) {
			this.probes = (IProbeArray<?>) dataObject;
		} else {
			this.probes = ProbeArrayService.newProbeArray(dataObject);
		}
	}

	/**
	 * Creates a new {@link ExecutionData} object with the given probe data
	 * length. All probes are set to <code>false</code>.
	 * 
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name
	 * @param probeCount
	 *            probe count
	 */
	public ExecutionData(final long id, final String name, final int probeCount) {
		this.id = id;
		this.name = name;
		this.probes = ProbeArrayService.newProbeArray(probeCount);
	}

	/**
	 * Returns a deep copy of this execution data
	 * 
	 * @return a deep copy of this execution data
	 */
	public ExecutionData deepCopy() {
		return new ExecutionData(id, name, probes.copy());
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
	 * @return probe data
	 */
	public IProbes getProbes() {
		return probes;
	}

	/**
	 * Sets all probes to <code>0</code>.
	 */
	public void reset() {
		probes.reset();
	}

	/**
	 * Merges the given execution data into the probe data of this object. I.e.
	 * a probe entry in this object is marked as executed (<code>true</code>) if
	 * this probe or the corresponding other probe was executed. So the result
	 * is
	 * 
	 * <pre>
	 * A or B
	 * </pre>
	 * 
	 * The probe array of the other object is not modified.
	 * 
	 * @param other
	 *            execution data to merge
	 */
	public void merge(final ExecutionData other) {
		merge(other, true);
	}

	/**
	 * Merges the given execution data into the probe data of this object. A
	 * probe in this object is set to the value of <code>flag</code> if the
	 * corresponding other probe was executed. For <code>flag==true</code> this
	 * corresponds to
	 * 
	 * <pre>
	 * A or B
	 * </pre>
	 * 
	 * For <code>flag==true</code> this can be considered as a subtraction
	 * 
	 * <pre>
	 * A and not B
	 * </pre>
	 * 
	 * The probe array of the other object is not modified.
	 * 
	 * @param other
	 *            execution data to merge
	 * @param flag
	 *            merge mode
	 */
	public void merge(final ExecutionData other, final boolean flag) {
		assertCompatibility(other.getId(), other.getName(), other.probes);
		probes.merge(other.probes, flag);
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
	 * @param other
	 *            probe data, must be the same type and length as for this data
	 * @throws IllegalStateException
	 *             if the given parameters do not match this instance
	 */
	public void assertCompatibility(final long id, final String name,
			final IProbeArray<?> other) throws IllegalStateException {
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
		probes.assertCompatibility(other);
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
	 * @param probecount
	 *            probe data length, must be the same as for this data
	 * @throws IllegalStateException
	 *             if the given parameters do not match this instance
	 */
	public void assertCompatibility(final long id, final String name,
			final int probecount) throws IllegalStateException {
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
		if (probes.length() != probecount) {
			throw new IllegalStateException(format(
					"Incompatible execution data for class %s with id %016x.",
					name, Long.valueOf(id)));
		}
	}

	@Override
	public String toString() {
		return String.format("ExecutionData[name=%s, id=%016x]", name,
				Long.valueOf(id));
	}
}
