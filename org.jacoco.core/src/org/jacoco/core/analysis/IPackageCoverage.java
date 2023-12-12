/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.util.Collection;

/**
 * Coverage data of a Java package containing classes and source files. The name
 * of this node is the package name in VM notation (slash separated). The name
 * of the default package is the empty string.
 *
 * @see IClassCoverage
 * @see ISourceFileCoverage
 */
public interface IPackageCoverage extends ICoverageNode {

	/**
	 * Returns all classes contained in this package.
	 *
	 * @return all classes
	 */
	Collection<IClassCoverage> getClasses();

	/**
	 * Returns all source files in this package.
	 *
	 * @return all source files
	 */
	Collection<ISourceFileCoverage> getSourceFiles();

}
