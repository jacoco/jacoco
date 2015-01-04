/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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
	public Collection<IClassCoverage> getClasses();

	/**
	 * Returns all source files in this package.
	 * 
	 * @return all source files
	 */
	public Collection<ISourceFileCoverage> getSourceFiles();

}