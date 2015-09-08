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
package org.jacoco.core.data;

/**
 * All probe types implement this interface for use by non-internal code.
 * 
 * @author Omer Azmon
 */
public interface IProbes {

	/**
	 * Returns the number of probes in this object
	 * 
	 * @return the number of probes in this object
	 */
	public int length();

	/**
	 * Does a probe have coverage.
	 * 
	 * @param index
	 *            the probe to return.
	 * 
	 * @return {@code true} if it does; Otherwise, {@code false}.
	 */
	public boolean isProbeCovered(int index);

	/**
	 * Get a coverage count of a probe.
	 * 
	 * @param index
	 *            the probe to return.
	 * 
	 * @return the coverage count of the probe.
	 */
	public int getCoverageProbe(int index);

	/**
	 * Get a parallel coverage probes.
	 * 
	 * @param index
	 *            the probe to return.
	 * 
	 * @return the parallel coverage count of the probe.
	 */
	public int getParallelCoverageProbe(int index);

}