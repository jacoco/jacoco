/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis.filter;

import org.jacoco.core.analysis.IClassCoverage;

/**
 * Determines whether a {@link IClassCoverage} should be included in the report
 */
public interface ICoverageFilter {
	/**
	 * Should the {@link IClassCoverage} be included in the report
	 * 
	 * @param coverage
	 * 
	 * @return true if the coverage should be included, false if not
	 */
	boolean shouldInclude(IClassCoverage coverage);
}