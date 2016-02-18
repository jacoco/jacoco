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
package org.jacoco.core.analysis;

import java.util.Collection;

import org.jacoco.core.tools.LoggingBridge;

/**
 * Coverage data of a bundle. A bundle groups a collection of packages.
 * 
 * @see IPackageCoverage
 */
public interface IBundleCoverage extends ICoverageNode {

	/**
	 * Returns all packages contained in this bundle.
	 * 
	 * @return all packages
	 */
	public Collection<IPackageCoverage> getPackages();

	/**
	 * Log missing debug info, if missing. This is so we have common behavior
	 * among different users, such as Ant, Maven, Direct Generation.
	 * 
	 * @param log
	 *            a standard java logger. Only the info, warning, and severe
	 *            methods are used.
	 */
	public abstract void logMissingDebugInformation(final LoggingBridge log);

	/**
	 * Log the coverage info. This is so we have common behavior among different
	 * users, such as Ant, Maven, Direct Generation.
	 * 
	 * @param noMatchClasses
	 *            the no match classes list from CoverageBuilder
	 * @param log
	 *            a standard java logger. Only the info, warning, and severe
	 *            methods are used.
	 */
	public abstract void logCoverageInfo(
			final Collection<IClassCoverage> noMatchClasses,
			final LoggingBridge log);

}