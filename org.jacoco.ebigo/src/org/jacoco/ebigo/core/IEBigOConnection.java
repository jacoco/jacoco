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
package org.jacoco.ebigo.core;

import java.io.IOException;

/**
 * A JaCoCo agent connection that can be used to collect workload coverage
 * information for use in Empirical Big-O analysis. The agent's output mode mus
 * be 'tcpserver'. See package-info for more details on setting up the agent.
 */
public interface IEBigOConnection {

	/**
	 * Collect workload coverage information. The coverage information is since
	 * the agent has started or the last reset. One should invoke the @{code
	 * reset} method of this class just before starting a workload to ensure the
	 * purity of the coverage data.
	 * 
	 * @param attributeMap
	 *            a map of X-axis attributes and values that will be associated
	 *            with this workload.
	 * @return the workload
	 * @throws IOException
	 *             on any communication failure
	 */
	public abstract EmpiricalBigOWorkload fetchWorkloadCoverage(
			WorkloadAttributeMap attributeMap) throws IOException;

	/**
	 * Collect workload coverage information. The coverage information is since
	 * the agent has started or the last reset. One should invoke the @{code
	 * reset} method of this class just before starting a workload to ensure the
	 * purity of the coverage data. The attribute used is the
	 * {@code WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE}
	 * 
	 * @param attributeValue
	 *            the value of the attribute named
	 * @return the workload
	 * @throws IOException
	 *             on any communication failure
	 */
	public abstract EmpiricalBigOWorkload fetchWorkloadCoverage(
			int attributeValue) throws IOException;

	/**
	 * Collect workload coverage information. The coverage information is since
	 * the agent has started or the last reset. One should invoke the @{code
	 * reset} method of this class just before starting a workload to ensure the
	 * purity of the coverage data.
	 * 
	 * @param attributeName
	 *            the name of a single X-axis attribute that will be associated
	 *            with this workload.
	 * @param attributeValue
	 *            the value of the attribute named
	 * @return the workload
	 * @throws IOException
	 *             on any communication failure
	 */
	public abstract EmpiricalBigOWorkload fetchWorkloadCoverage(
			String attributeName, int attributeValue) throws IOException;

	/**
	 * Reset all coverage counters in the agent to zero.
	 * 
	 * @throws IOException
	 *             on any communication failure
	 */
	public abstract void resetCoverage() throws IOException;

	/**
	 * Close the connection. Once closed, it cannot be reopened. A new
	 * connection must be established.
	 * 
	 * @throws IOException
	 *             on any failure to close the underlying socket
	 */
	public abstract void close() throws IOException;

}