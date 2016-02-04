/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

/**
 * All probe types implement this interface for use by non-internal code.
 * 
 * @author Omer Azmon
 */
public interface IProbes {

	/**
	 * Get the type of this probe
	 * 
	 * @return the type of this probe
	 */
	public ProbeMode getProbeMode();

	/**
	 * Returns the number of probes in this object
	 * 
	 * @return the number of probes in this object
	 */
	public int length();

	/**
	 * Increment a probe. The method is for external users that are manipulating
	 * execution data.
	 * 
	 * @param probeId
	 *            the probe id must be between zero and the number of probes
	 *            given at construction.
	 */
	public void increment(final int probeId);

	/**
	 * Does a probe have coverage.
	 * 
	 * @param index
	 *            the probe to return.
	 * 
	 * @return {@code true} if it does; Otherwise, {@code false}.
	 */
	public boolean isProbeCovered(final int index);

	/**
	 * Get a coverage count of a probe.
	 * 
	 * @param index
	 *            the probe to return.
	 * 
	 * @return the coverage count of the probe.
	 */
	public int getExecutionProbe(final int index);

	/**
	 * Get a parallel coverage probes.
	 * 
	 * @param index
	 *            the probe to return.
	 * 
	 * @return the parallel coverage count of the probe.
	 */
	public int getParallelExecutionProbe(final int index);

}