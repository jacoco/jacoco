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
package org.jacoco.ebigo.analysis;

import org.jacoco.core.analysis.IClassCoverage;

/**
 * Empirical big-o data of a single class containing methods. The name of this
 * node is the fully qualified class name in VM notation (slash separated).
 * 
 * @see IClassCoverage
 */
public interface IClassEmpiricalBigO {

	/**
	 * Returns the {@code IClassCoverage[]} array of matched class coverage for a single class, in X-value
	 * order (from the analysis).
	 * 
	 * @return workload coverage classes f this class
	 */
	public IClassCoverage[] getMatchedCoverageClasses();
}
